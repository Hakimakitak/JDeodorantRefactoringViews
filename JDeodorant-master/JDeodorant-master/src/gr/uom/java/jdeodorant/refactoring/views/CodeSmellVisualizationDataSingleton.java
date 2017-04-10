package gr.uom.java.jdeodorant.refactoring.views;

import java.util.ArrayList;

import gr.uom.java.ast.visualization.GodClassVisualizationData;
import gr.uom.java.ast.visualization.VisualizationData;
import gr.uom.java.distance.CandidateRefactoring;

public class CodeSmellVisualizationDataSingleton {
	private static VisualizationData data;
	private static CandidateRefactoring[] candidates;
	private static ArrayList<GodClassVisualizationData> godClassData = new ArrayList<GodClassVisualizationData>();
	
	public static ArrayList<GodClassVisualizationData> getGodClasses(){
		return godClassData;
	}
	
	public static boolean addGodClass(GodClassVisualizationData data){
		if(godClassData.contains(data)) {
			return false;
		}
		godClassData.add(data);
		return true;
	}
	
	public static boolean removeGodClass(GodClassVisualizationData data){
		return godClassData.remove(data);
	}
	
	public static int countGodClasses(){
		return godClassData.size();
	}
	
	public static CandidateRefactoring[] getCandidates() {
		return candidates;
	}

	public static void setCandidates(CandidateRefactoring[] candidates) {
		CodeSmellVisualizationDataSingleton.candidates = candidates;
	}

	public static VisualizationData getData() {
		return data;
	}

	public static void setData(VisualizationData data) {
		CodeSmellVisualizationDataSingleton.data = data;
	}
}
