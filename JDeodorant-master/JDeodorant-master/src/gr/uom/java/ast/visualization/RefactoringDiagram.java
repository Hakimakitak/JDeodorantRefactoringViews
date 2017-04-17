package gr.uom.java.ast.visualization;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;


import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.FieldObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.jdeodorant.refactoring.views.CodeSmellVisualizationDataSingleton;

public class RefactoringDiagram {

	private ScalableFreeformLayeredPane root;
	private FreeformLayer primary;
	private ConnectionLayer connections;
	private List<JConnection> connectionList= new ArrayList<JConnection>();
	private int bendGap;
	
	int sourceClassWidth = 200;
	int targetClassWidth = 200;
	int classWidth = 300;
	int targetSectionWidth = targetClassWidth/3;
	int startPointX = 100;
	int startPointY = 50;
	int curPointX = startPointX;
	int curPointY = startPointY;
	int curGridX = 0;
	int curGridY = 0;
	int maxGridX = 2;
	int maxGridY = 0;
	int gridXInc = 3;
	int gridYInc = 1;
	int gap = 300;
	int xGap = 150;
	int yGap = 50;
	int curY = 0;
	
	public RefactoringDiagram(){
		root = new ScalableFreeformLayeredPane();
		primary = new FreeformLayer();
		primary.setLayoutManager(new FreeformLayout());
		root.setFont(Display.getDefault().getSystemFont());
		root.add(primary,"Primary");
		connections = new ConnectionLayer();
		
		//for each god class create a source class and a line to an extracted class
		if(CodeSmellVisualizationDataSingleton.countGodClasses() > 0){
			/*
			ClassFigure extractedClasses = new ClassFigure("Extracted Classes", DecorationConstants.classColor);
			extractedClasses.setToolTip(new Label("Extracted Classes"));
			//primary.add(extractedClasses, getNewClassRectangle());
			primary.add(extractedClasses, new Rectangle(startPointX, startPointY, classWidth, -1));
			curGridX = 1;
			*/
						
			ArrayList<GodClassVisualizationData> candidates = CodeSmellVisualizationDataSingleton.getGodClassData();
			HashMap<ClassObject, ClassFigure> activeClasses = new HashMap<ClassObject, ClassFigure>();
			HashMap<ClassFigure, ArrayList<GodClassVisualizationData>> activeFigures = new HashMap<ClassFigure, ArrayList<GodClassVisualizationData>>();
			
			for(GodClassVisualizationData candidate : candidates){
				ClassObject sourceClass = candidate.getSourceClass();
				if(!activeClasses.containsKey(sourceClass)){
					ClassFigure classFigure = new ClassFigure(candidate.getSourceClass().getClassName(), DecorationConstants.classColor);
					classFigure.setToolTip(new Label(candidate.getSourceClass().getName()));
					activeClasses.put(sourceClass, classFigure);
					ArrayList<GodClassVisualizationData> selectedCandidates = new ArrayList<GodClassVisualizationData>();
					selectedCandidates.add(candidate);
					activeFigures.put(classFigure, selectedCandidates);
				} else {
					ClassFigure classFigure = activeClasses.get(sourceClass);
					ArrayList<GodClassVisualizationData> selectedCandidates = activeFigures.get(classFigure);
					selectedCandidates.add(candidate);
					activeFigures.put(classFigure, selectedCandidates);
				}
			}
			
			Iterator it = activeFigures.entrySet().iterator();
		    while (it.hasNext()) {
		    	Map.Entry pair = (Map.Entry)it.next();
		        ClassFigure godClassfigure = (ClassFigure)pair.getKey();
		        
		        primary.add(godClassfigure, getNewClassRectangle(0, curY));
				ArrayList<GodClassVisualizationData> selectedCandidates = (ArrayList<GodClassVisualizationData>) pair.getValue();
		        
				String extractedClasslabel;
		        if(selectedCandidates.size() == 1) extractedClasslabel = "Extracted Class";
		        else extractedClasslabel = selectedCandidates.size() + " Extracted Classes";
		        ClassFigure extractedClassFigure = new ClassFigure(extractedClasslabel, DecorationConstants.classColor);
		        extractedClassFigure.setToolTip(new Label("Extracted Class"));
		        primary.add(extractedClassFigure, getNewClassRectangle(1, curY));
				curY++;        
		        
				//identify any conflicts
				Set<GodClassVisualizationData> validCandidates = new HashSet<GodClassVisualizationData>();
				Set<GodClassVisualizationData> invalidCandidates = new HashSet<GodClassVisualizationData>();
				Set<MethodObject> allMethods = new HashSet<MethodObject>();
				Set<MethodObject> problemMethods = new HashSet<MethodObject>();
				Set<FieldObject> allFields = new HashSet<FieldObject>();
				Set<FieldObject> problemFields = new HashSet<FieldObject>();
				for(GodClassVisualizationData candidate : selectedCandidates){
					boolean validCandidate = true;
					Set<MethodObject> methods = candidate.getExtractedMethods();
					for(MethodObject method : methods){
						if(allMethods.contains(method)){
							validCandidate = false;
							problemMethods.add(method);
						}
						allMethods.add(method);
					}
					
					Set<FieldObject> fields = candidate.getExtractedFields();
					for(FieldObject field : fields){
						if(allFields.contains(field)){
							validCandidate = false;
							problemFields.add(field);
						}
						allFields.add(field);
					}
					
					if(validCandidate) validCandidates.add(candidate);
					else invalidCandidates.add(candidate);
				}
				
				//String label = validCandidates.size() + "/" + (selectedCandidates.size());
				String label = (problemFields.size()+problemMethods.size()) + "\\" + (allFields.size()+allMethods.size()); 
				String toolTip = "Methods Extracted to Multiple Classes:";
				for(MethodObject m : problemMethods){
					toolTip += "\n- " + m.getName();
				}
				toolTip += "\n\nFields Extracted to Multiple Classes:";
				for(FieldObject f : problemFields){
					toolTip += "\n- " + f.getName();
				}
						
						
				JConnection connection = godClassfigure.addRightLeftConnection(ConnectionType.READ_FIELD_TARGET, extractedClassFigure, label, toolTip);
				connection.setReadStyle();
				connections.add(connection);		
				it.remove(); // avoids a ConcurrentModificationException
		    }
		    root.add(connections, "Connections");
		}
	}
	
	Rectangle getNewClassRectangle(int curX, int curY){
		int x = startPointX + (curX * (classWidth + xGap));
		int y = startPointY + (curY * yGap);
		
		//Update this so something more visually appealing later
		//if(curGridX > curGridY) curGridY++;
		//else curGridX++;
		//curGridY++;
		
		return new Rectangle(x, y, classWidth, -1);
	}

	public ScalableFreeformLayeredPane getRoot() {
		// TODO Auto-generated method stub
		return root;
	}

	
}
