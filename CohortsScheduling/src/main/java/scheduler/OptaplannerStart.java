package scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.optaplanner.core.api.solver.Solver;
import org.optaplanner.core.api.solver.SolverFactory;

import CohortDataClasses.Cohort;
import CohortDataClasses.CohortSectionAssignment;
import CohortDataClasses.Section;
import CohortScoring.cohortScoring;
import CohortsSolverData.CohortSolution;
public class OptaplannerStart implements Runnable {
	private CohortSolution solutions[];
	private boolean finished;
	public void run() {
		finished = false;
		scheduleRunner(solutions);
	}
	
	public boolean isFinished() {
		return finished;
	}
	
	public OptaplannerStart(CohortSolution solutions[]) {
		this.solutions = solutions;
	}
	
	public static void scheduleRunner(CohortSolution solutions[])
    {
    	try {
    		
    		SolverFactory<CohortSolution> factory = SolverFactory.createFromXmlResource("SolverConfig.xml");
    		Solver<CohortSolution> solver = factory.buildSolver();
    		cohortScoring score = new cohortScoring();
    		for(int i = 0; i<1 ;i++){
    			System.out.println("Before: " + score.calculateScore(solutions[i]));
    			solutions[i] = (CohortSolution)solver.solve(solutions[i]);
    			System.out.println("After: " + score.calculateScore(solutions[i]));
    		}
    		recordSolutions(solutions);
    	}catch(Exception e) {
    		e.printStackTrace();
    	}
    	
    	
    	
    }

	private static void recordSolutions(CohortSolution[] solutions) {
		for(CohortSolution s: solutions) {
			System.out.println(s.getAssignments().get(0).getAssignment().getName());
			System.out.println("\nSolution");
			List<Cohort> cohorts = putAssignmentsInCohorts(s);
			for(Cohort c: cohorts) {
				System.out.println("Cohort "+c.getName()+" classes:");
				for(Section sect:c.getClassAssignments()) {
					if(sect.getStartTime()!=null) {
						System.out.println("Class: "+sect.getName()
						+" Section: "+sect.getSectionId()
						+" Days: "+sect.getDaysOfWeek()
						+" Time: "+ sect.getStartTime().toString()
						+" - "+sect.getEndTime().toString());
					}else {
						System.out.println("Class: "+sect.getName()
						+" Section: "+sect.getSectionId()
						+" Days: "+sect.getDaysOfWeek()
						+" ONLINE");
					}
						
				}
			}
		}
		
	}
	private static List<Cohort> putAssignmentsInCohorts(CohortSolution solution) {
		Map<String,List<Section>> sectMap = new HashMap<>();
		for(CohortSectionAssignment csa: solution.getAssignments()) {
			if(sectMap.containsKey(csa.getMyCohort().getName())) {
				List<Section> temp = sectMap.get(csa.getMyCohort().getName());
				temp.add(csa.getAssignment());
				sectMap.put(csa.getMyCohort().getName(),temp);
			}else {
				List<Section> temp = new ArrayList<>();
				temp.add(csa.getAssignment());
				sectMap.put(csa.getMyCohort().getName(), temp);
			}
		}
		List<String> cohortNames = new ArrayList<String>(sectMap.keySet());
		List<Cohort> cohorts = new ArrayList<>();
		for(String name:cohortNames) {
			Cohort coh = new Cohort();
			coh.setName(name);
			coh.setClassAssignments(sectMap.get(name));
			cohorts.add(coh);
		}
		return cohorts;
	}
}
