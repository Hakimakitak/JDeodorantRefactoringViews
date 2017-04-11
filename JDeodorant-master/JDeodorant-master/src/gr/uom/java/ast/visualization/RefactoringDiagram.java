package gr.uom.java.ast.visualization;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
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
import gr.uom.java.jdeodorant.refactoring.views.CodeSmellVisualizationDataSingleton;

public class RefactoringDiagram {

	private ScalableFreeformLayeredPane root;
	private FreeformLayer primary;
	private ConnectionLayer connections;
	private List<JConnection> connectionList= new ArrayList<JConnection>();
	private int bendGap;
	
	int sourceClassWidth = 200;
	int targetClassWidth = 200;
	int classWidth = 200;
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
	
	public RefactoringDiagram(){
		root = new ScalableFreeformLayeredPane();
		primary = new FreeformLayer();
		primary.setLayoutManager(new FreeformLayout());
		root.setFont(Display.getDefault().getSystemFont());
		root.add(primary,"Primary");
		connections = new ConnectionLayer();
		
		//if there are god classes, make an Extracted Class class 
		//for each god class create a source class. arrange them 3x1
		//for each extraction, add a line to extract. for now we'll show no information
		if(CodeSmellVisualizationDataSingleton.countGodClasses() > 0){
			ClassFigure extractedClasses = new ClassFigure("Extracted Classes", DecorationConstants.classColor);
			extractedClasses.setToolTip(new Label("Extracted Classes"));
			//primary.add(extractedClasses, getNewClassRectangle());
			primary.add(extractedClasses, new Rectangle(startPointX, startPointY, classWidth, -1));
			curGridX = 1;
						
			ArrayList<GodClassVisualizationData> candidates = CodeSmellVisualizationDataSingleton.getGodClasses();
			HashMap<ClassObject, ClassFigure> activeClasses = new HashMap<ClassObject, ClassFigure>();
			HashMap<ClassFigure, Integer> activeFigures = new HashMap<ClassFigure, Integer>();
			
			for(GodClassVisualizationData candidate : candidates){
				ClassObject sourceClass = candidate.getSourceClass();
				if(!activeClasses.containsKey(sourceClass)){
					ClassFigure classFigure = new ClassFigure(candidate.getSourceClass().getName(), DecorationConstants.classColor);
					classFigure.setToolTip(new Label(candidate.getSourceClass().getName()));
					activeClasses.put(sourceClass, classFigure);
					activeFigures.put(classFigure, 1);
				} else {
					ClassFigure classFigure = activeClasses.get(sourceClass);
					int curRefactors = activeFigures.get(classFigure);
					curRefactors++;
					activeFigures.put(classFigure, curRefactors);
				}
			}
			Iterator it = activeFigures.entrySet().iterator();
		    while (it.hasNext()) {
		    	Map.Entry pair = (Map.Entry)it.next();
		        ClassFigure figure = (ClassFigure)pair.getKey();
		        primary.add(figure, getNewClassRectangle());
				JConnection connection = figure.addLeftRightConnection(ConnectionType.READ_FIELD_TARGET, extractedClasses, (Integer) pair.getValue());
				connection.setReadStyle();
				connections.add(connection);		
				it.remove(); // avoids a ConcurrentModificationException
		    }
		    root.add(connections, "Connections");
		}
	}
	
	Rectangle getNewClassRectangle(){
		int x = startPointX + (curGridX * (classWidth + xGap));
		int y = startPointY + (curGridY * yGap);
		
		//Update this so something more visually appealing later
		//if(curGridX > curGridY) curGridY++;
		//else curGridX++;
		curGridY++;
		
		return new Rectangle(x, y, classWidth, -1);
	}

	public ScalableFreeformLayeredPane getRoot() {
		// TODO Auto-generated method stub
		return root;
	}

	
}
