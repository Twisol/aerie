package gov.nasa.jpl.ammos.mpsa.aerie.plan.controllers;

import gov.nasa.jpl.ammos.mpsa.aerie.plan.exceptions.NoSuchActivityInstanceException;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.exceptions.NoSuchPlanException;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.exceptions.ValidationException;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.ActivityInstance;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.NewPlan;
import gov.nasa.jpl.ammos.mpsa.aerie.plan.models.Plan;
import org.apache.commons.lang3.tuple.Pair;

import java.util.stream.Stream;

public interface IPlanController {
  Stream<Pair<String, Plan>> getPlans();
  Plan getPlanById(String id) throws NoSuchPlanException;
  String addPlan(NewPlan plan) throws ValidationException;
  void removePlan(String id) throws NoSuchPlanException;
  void updatePlan(String id, Plan patch) throws ValidationException, NoSuchPlanException;
  void replacePlan(String id, NewPlan plan) throws ValidationException, NoSuchPlanException;
  ActivityInstance getActivityInstanceById(String planId, String activityInstanceId) throws NoSuchPlanException, NoSuchActivityInstanceException;
}