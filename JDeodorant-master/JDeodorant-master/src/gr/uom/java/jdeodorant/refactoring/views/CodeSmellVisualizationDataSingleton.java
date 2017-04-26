package gr.uom.java.jdeodorant.refactoring.views;

import java.util.ArrayList;

import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import gr.uom.java.ast.visualization.FeatureEnvyVisualizationData;
import gr.uom.java.ast.visualization.GodClassVisualizationData;
import gr.uom.java.ast.visualization.VisualizationData;
import gr.uom.java.distance.CandidateRefactoring;

public class CodeSmellVisualizationDataSingleton {
	private static VisualizationData data;
	private static CandidateRefactoring[] candidates;
	private static ArrayList<GodClassVisualizationData> godClassData = new ArrayList<GodClassVisualizationData>();
	private static ArrayList<FeatureEnvyVisualizationData> featureEnvyData = new ArrayList<FeatureEnvyVisualizationData>();
	
	public static boolean displayRefactoringDiagram = false;
	
	
	public static ArrayList<GodClassVisualizationData> getGodClassData(){
		return godClassData;
	}
	
	public static ArrayList<FeatureEnvyVisualizationData> getFeatureEnvyData(){
		return featureEnvyData;
	}
	
	public static boolean addGodClass(GodClassVisualizationData data){
		if(godClassData.contains(data)) {
			return false;
		}
		godClassData.add(data);
		return true;
	}
	
	public static boolean addFeatureEnvy(FeatureEnvyVisualizationData data){
		if(featureEnvyData.contains(data)){
			return false;
		}
		featureEnvyData.add(data);
		return true;
	}
	
	public static boolean removeGodClass(GodClassVisualizationData data){
		return godClassData.remove(data);
	}
	
	public static boolean removeFeatureEnvy(FeatureEnvyVisualizationData data){
		return featureEnvyData.remove(data);
	}
	
	public static int countGodClasses(){
		return godClassData.size();
	}
	
	public static int countFeatureEnvy(){
		return featureEnvyData.size();
	}
	
	public static int countAllCandidates(){
		return godClassData.size() + featureEnvyData.size();
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

	public static void resetSelectedCandidates() {
		godClassData = new ArrayList<GodClassVisualizationData>();
		featureEnvyData = new ArrayList<FeatureEnvyVisualizationData>();
	}
	
	public static void resetGodClassData(){
		godClassData = new ArrayList<GodClassVisualizationData>();
	}
	
	public static void resetFeatureEnvyData(){
		featureEnvyData = new ArrayList<FeatureEnvyVisualizationData>();
	}
	
	public static MessageConsole findConsole(String name) {
	      ConsolePlugin plugin = ConsolePlugin.getDefault();
	      IConsoleManager conMan = plugin.getConsoleManager();
	      IConsole[] existing = conMan.getConsoles();
	      for (int i = 0; i < existing.length; i++)
	         if (name.equals(existing[i].getName()))
	            return (MessageConsole) existing[i];
	      //no console found, so create a new one
	      MessageConsole myConsole = new MessageConsole(name, null);
	      conMan.addConsoles(new IConsole[]{myConsole});
	      return myConsole;
	   }
}
