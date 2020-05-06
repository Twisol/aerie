package gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.processor;

import com.fasterxml.jackson.core.JsonFactory;
import com.squareup.javapoet.JavaFile;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivitiesMapped;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivityType;
import gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ParameterType;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@SupportedAnnotationTypes({
    "gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivityType",
    "gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ParameterType",
    "gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.Parameter",
    "gov.nasa.jpl.ammos.mpsa.aerie.merlinsdk.activities.annotations.ActivitiesMapped"
})
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public final class ActivityProcessor extends AbstractProcessor {
  private TypeInfoMaker infoMaker = null;
  private MapperMaker mapperMaker = null;
  private final JsonFactory jsonFactory = new JsonFactory();

  // The set of the names of all activity types seen so far.
  private final Map<TypeMirror, ActivityTypeInfo> activityTypesFound = new HashMap<>();
  private final Map<TypeMirror, TypeMirror> activityTypesWithMappers = new HashMap<>();

  @Override
  public void init(final ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    this.infoMaker = new TypeInfoMaker(processingEnv);
    this.mapperMaker = new MapperMaker(processingEnv);
  }

  private void writeActivityMetadata(final FileObject resourceFile, final ActivityTypeInfo activityTypeInfo) throws IOException {
    try (final var writer = resourceFile.openWriter()) {
      final var generator = jsonFactory.createGenerator(writer);
      generator.writeStartObject(activityTypeInfo);
      generator.writeStringField("name", activityTypeInfo.name);
      generator.writeStringField("contact", activityTypeInfo.contact);
      generator.writeStringField("subsystem", activityTypeInfo.subsystem);
      generator.writeStringField("brief_description", activityTypeInfo.briefDescription);
      generator.writeStringField("verbose_description", activityTypeInfo.verboseDescription);
      generator.writeEndObject();
      generator.close();
    }
  }

  private boolean shouldWriteDictionary(
      final Set<? extends TypeElement> annotations,
      final RoundEnvironment roundEnv
  ) {
    // NOTE:
    //   Morally, `useLastRound` should always be true. However, a curious behavior
    //   of OpenJDK 12 is that code produced during the final round of processing
    //   does not influence identifier resolution. Thus, if another file in the
    //   project references the class to be created by this processor, a "cannot
    //   find symbol" error would be produced, even though that class has clearly
    //   just been generated. Emitting the class earlier in the process avoids this,
    //   but is technically incorrect if classes with our annotations are themselves
    //   being generated by another processor. (Such classes would trigger another
    //   round of processing by us, resulting in an attempt to illegally recreate
    //   the helper classes generated in a previous round.)
    //
    //   We default to the morally-correct behavior, since the user can perform a
    //   second compilation (which should succeed due to the code generated the first
    //   time), but allow the users of this processor to override this behavior.
    final boolean useLastRound = Boolean.parseBoolean(
        this.processingEnv.getOptions().getOrDefault("merlin.lastRound", "true"));

    if (useLastRound) {
      return roundEnv.processingOver();
    } else {
      return (!annotations.isEmpty() && !roundEnv.processingOver());
    }
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
    // Collect any parameter types produced in the previous round.
    for (final Element element : roundEnv.getElementsAnnotatedWith(ParameterType.class)) {
      // Extract information about this parameter type.
      final ParameterTypeInfo parameterTypeInfo;
      try {
        parameterTypeInfo = this.infoMaker.getParameterInfo(element);
      }
      catch (final ParameterTypeException ex) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getRelatedElement());
        continue;
      }

      // TODO: Generate a mapper for this parameter type.
    }

    // Collect any activity types produced in the previous round.
    for (final Element element : roundEnv.getElementsAnnotatedWith(ActivityType.class)) {
      // Extract information about this activity type.
      final ActivityTypeInfo activityTypeInfo;
      try {
        activityTypeInfo = this.infoMaker.getActivityInfo(element);
      }
      catch (final ParameterTypeException ex) {
        this.processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, ex.getMessage(), ex.getRelatedElement());
        continue;
      }

      // If this activity type's name collides with another's, report that and bail.
      if (this.activityTypesFound.containsValue(activityTypeInfo.name)) {
        this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Multiple activity types found with name `" + activityTypeInfo.name + "`",
                activityTypeInfo.javaType.asElement());
        continue;
      }
      else {
        this.activityTypesFound.put(element.asType(), activityTypeInfo);
      }

      // Synthesize a class for serializing and de-serializing this activity type.
      if (activityTypeInfo.needsGeneratedMapper) {
        final JavaFile file = this.mapperMaker.makeActivityMapper(activityTypeInfo);
        try {
          file.writeTo(this.processingEnv.getFiler());
        }
        catch (final IOException e) {
          this.processingEnv.getMessager().printMessage(
                  Diagnostic.Kind.ERROR,
                  "Unable to open file to generate class `" + file.packageName + "." + file.typeSpec.name + "` -- does it already exist?",
                  activityTypeInfo.javaType.asElement());
          continue;
        }
      }

      // Save activity type metadata in a META-INF collection.
      try {
        final FileObject resourceFile = this.processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/merlin/activities/" + activityTypeInfo.name,
                activityTypeInfo.javaType.asElement()
        );
        writeActivityMetadata(resourceFile, activityTypeInfo);
      }
      catch (final IOException e) {
        this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unable to write activity metadata for activity `" + activityTypeInfo.name + "` -- does it already exist?",
                activityTypeInfo.javaType.asElement());
        continue;
      }
    }

    // Take note of any activity mappers. This gets ugly because Java isn't great for handling
    // annotations with class values
    final TypeMirror activitiesMappedType = processingEnv.getElementUtils().getTypeElement(ActivitiesMapped.class.getName()).asType();
    for (final Element element : roundEnv.getElementsAnnotatedWith(ActivitiesMapped.class)) {
      for (final AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
        if (annotationMirror.getAnnotationType().equals(activitiesMappedType)) {
          List<TypeMirror> newActivitiesWithMappers = getActivitiesMappedBy(annotationMirror);
          for (TypeMirror mappedActivity : newActivitiesWithMappers) {
            if (activityTypesWithMappers.containsKey(mappedActivity)) {
              processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "More than one activity mapper found for activity type " + mappedActivity.toString(), element);
            } else {
              activityTypesWithMappers.put(mappedActivity, element.asType());
            }
          }
        }
      }
    }

    if (roundEnv.processingOver()) {
      // TODO: Check that every activity/parameter type references only other parameter types.
      //       This may include primitives like double / Double.

      // Check that every activity type has one and only one associated mapper
      for (ActivityTypeInfo activityTypeInfo : activityTypesFound.values()) {
        TypeMirror className = activityTypeInfo.javaType;
        if (!activityTypesWithMappers.containsKey(activityTypeInfo.javaType)) {
          processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "No mapper specified for activity type " + className, activityTypeInfo.javaType.asElement());
        }
      }

      // Save activity type mappers in a META-INF collection.
      try {
        final FileObject resourceFile = this.processingEnv.getFiler().createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                "META-INF/merlin/activityMappers.json"
        );
        final var writer = resourceFile.openWriter();
        final var generator = jsonFactory.createGenerator(writer);
        generator.writeStartObject();
        for (final var entry : activityTypesWithMappers.entrySet()) {
          generator.writeStringField(activityTypesFound.get(entry.getKey()).name, entry.getValue().toString());
        }
        generator.writeEndObject();
        generator.close();
      }
      catch (final IOException e) {
        this.processingEnv.getMessager().printMessage(
                Diagnostic.Kind.ERROR,
                "Unable to write activity type mapper data."
        );
      }
    }
    return true;
  }

  private List<TypeMirror> getActivitiesMappedBy(AnnotationMirror annotationMirror) {
    List<TypeMirror> activityTypesWithMappers = new ArrayList<>();
    for (var entry : annotationMirror.getElementValues().entrySet()) {
      if ("value".equals(entry.getKey().getSimpleName().toString())) {
        List<? extends AnnotationValue> mappedClasses = (List<? extends AnnotationValue>)entry.getValue().getValue();
        for (var mappedClass : mappedClasses) {
          TypeMirror mappedClassMirror = (TypeMirror)mappedClass.getValue();
          activityTypesWithMappers.add(mappedClassMirror);
        }
      }
    }
    return activityTypesWithMappers;
  }
}
