package gr.uom.java.ast.visualization;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MoveAction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.ToolTipHelper;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.widgets.Display;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.FieldObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.distance.ExtractClassCandidateRefactoring;
import gr.uom.java.jdeodorant.refactoring.views.CodeSmellVisualizationDataSingleton;

public class RefactoringDiagram {

	private ScalableFreeformLayeredPane root;
	private FreeformLayer primary;
	private ConnectionLayer connections;
	private int bendGap;

	int sourceClassWidth = 200;
	int targetClassWidth = 200;
	int classWidth = 300;
	int targetSectionWidth = targetClassWidth/3;
	int startGridX = 100;
	int startGridY = 50;
	int curGridX = 0;
	int curGridY = 0;
	int maxGridX = 1;
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
		
		
		if(CodeSmellVisualizationDataSingleton.countAllCandidates() > 0){
					
			ArrayList<GodClassVisualizationData> godClassCandidates = CodeSmellVisualizationDataSingleton.getGodClassData();
			ArrayList<FeatureEnvyVisualizationData> featureEnvyCandidates = CodeSmellVisualizationDataSingleton.getFeatureEnvyData();
			
			HashMap<ClassObject, ClassFigure> classFigureMap = new HashMap<ClassObject, ClassFigure>();
			HashMap<ClassObject, HashSet<GodClassVisualizationData>> godClassRefactoringsMap = new HashMap<ClassObject, HashSet<GodClassVisualizationData>>();
			HashMap<ClassObject, HashSet<FeatureEnvyVisualizationData>> featureEnvyClassRefactoringsMap = new HashMap<ClassObject, HashSet<FeatureEnvyVisualizationData>>();
			
			HashSet<ClassObject> drawnClasses = new HashSet<ClassObject>();
			HashMap<ClassObject, ClassFigure> featureEnvyTargetClassFigures = new HashMap<ClassObject, ClassFigure>();
			
			
			for(GodClassVisualizationData candidate : godClassCandidates){
				ClassObject sourceClass = candidate.getSourceClass();
				if (!godClassRefactoringsMap.containsKey(sourceClass)){
					HashSet<GodClassVisualizationData> selectedCandidates = new HashSet<GodClassVisualizationData>();
					selectedCandidates.add(candidate);
					godClassRefactoringsMap.put(sourceClass, selectedCandidates);
				} else {
					HashSet<GodClassVisualizationData> selectedCandidates = godClassRefactoringsMap.get(sourceClass);
					selectedCandidates.add(candidate);
					godClassRefactoringsMap.put(sourceClass, selectedCandidates);
				}
				
				if(!classFigureMap.containsKey(sourceClass)){
					ClassFigure classFigure = new ClassFigure(sourceClass.getClassName(), DecorationConstants.classColor);
					classFigure.setToolTip(new Label(sourceClass.getName()));
					classFigureMap.put(sourceClass, classFigure);
				}
			}
			
			for(FeatureEnvyVisualizationData candidate : featureEnvyCandidates){
				ClassObject sourceClass = candidate.getSourceClass();
				if (!featureEnvyClassRefactoringsMap.containsKey(sourceClass)){
					HashSet<FeatureEnvyVisualizationData> selectedCandidates = new HashSet<FeatureEnvyVisualizationData>();
					selectedCandidates.add(candidate);
					featureEnvyClassRefactoringsMap.put(sourceClass, selectedCandidates);
				} else {
					HashSet<FeatureEnvyVisualizationData> selectedCandidates = featureEnvyClassRefactoringsMap.get(sourceClass);
					selectedCandidates.add(candidate);
					featureEnvyClassRefactoringsMap.put(sourceClass, selectedCandidates);
				}
				
				if(!classFigureMap.containsKey(sourceClass)){
					ClassFigure classFigure = new ClassFigure(sourceClass.getClassName(), DecorationConstants.classColor);
					classFigure.setToolTip(new Label(sourceClass.getName()));
					classFigureMap.put(sourceClass, classFigure);
				}
			}
			
			HashMap<ClassObject, int[]> classCoordinates = new HashMap<ClassObject, int[]>();
			
			//Iterate over each unique source class
			Iterator sourceIterator = classFigureMap.entrySet().iterator();
			while(sourceIterator.hasNext()){
				Map.Entry pair = (Map.Entry)sourceIterator.next();
				ClassObject sourceClass = (ClassObject)pair.getKey();
				ClassFigure sourceClassFigure = (ClassFigure)pair.getValue();
				
				//identify all conflicts (duplicates) across refactoring types
				//The interger refers to the # of occurrences
				//We need to use strings instead of MethodObjects, etc, because they include related classes in their equals/hash operations
				HashMap<String, Integer> extractedMethods = new HashMap<String, Integer>();
				HashMap<String, Integer> extractedFields = new HashMap<String, Integer>();
				HashMap<String, Integer> movedMethods = new HashMap<String, Integer>();
				HashMap<String, Integer> allRefactoredMethods = new HashMap<String, Integer>();
				HashMap<String, Integer> allRefactoredFields = new HashMap<String, Integer>();
				
				//collect and count each field/method refactoring occurrence
				if(godClassRefactoringsMap.containsKey(sourceClass)){
					for(GodClassVisualizationData refactor : godClassRefactoringsMap.get(sourceClass)){
						for(MethodObject methodObject : refactor.getExtractedMethods()){
							String method = methodObject.getSignature();
							if(extractedMethods.containsKey(method)){
								int count = extractedMethods.get(method);
								count++;
								extractedMethods.put(method, count);
							} else {extractedMethods.put(method, 1);}
							//
							if(allRefactoredMethods.containsKey(method)){
								int count = allRefactoredMethods.get(method);
								count++;
								allRefactoredMethods.put(method, count);
							} else {allRefactoredMethods.put(method, 1);}
						}
						for(FieldObject fieldObject : refactor.getExtractedFields()){
							String field = fieldObject.getName();
							if(extractedFields.containsKey(field)){
								int count = extractedFields.get(field);
								count++;
								extractedFields.put(field, count);
							} else extractedFields.put(field, 1);
						}
					}
				}
				
				

				if(featureEnvyClassRefactoringsMap.containsKey(sourceClass)){
					for(FeatureEnvyVisualizationData refactor : featureEnvyClassRefactoringsMap.get(sourceClass)){
						MethodObject methodObject = refactor.getMethodToBeMoved();
						String method = methodObject.getSignature();
						if(movedMethods.containsKey(method)){
							int count = movedMethods.get(method);
							count++;
							movedMethods.put(method, count);
						} else {movedMethods.put(method, 1);}
						//
						if(allRefactoredMethods.containsKey(method)){
							int count = allRefactoredMethods.get(method);
							count++;
							allRefactoredMethods.put(method, count);
						} else {allRefactoredMethods.put(method, 1);}
					}
				}
				
				
				String tempM = "\nMethods:\n";
				String tempF = "\nFields:\n";
				
				int validExtractClassEntities = 0;
			    int invalidExtractClassEntities = 0;
			    
				
				Iterator methodIterator = allRefactoredMethods.entrySet().iterator();
			    while (methodIterator.hasNext()) {
			        Map.Entry<String, Integer> methodPair = (Map.Entry)methodIterator.next();
			        String method = methodPair.getKey();
			        Integer methodCount = methodPair.getValue();
			        if(methodCount > 1){
			        	invalidExtractClassEntities++;
			        	tempM += method + ": ";
			        	boolean extracted = extractedMethods.containsKey(methodPair.getKey());
			        	boolean moved = movedMethods.containsKey(method);
			        	if(extracted){
			        		int extractCount = extractedMethods.get(method);
			        		if(extractCount == 1) tempM += extractCount + " Extract Class Refactor";
							else if(extractCount > 1) tempM += extractCount + " Extract Class Refactors";
			        		if(moved){
			        			tempM += ", ";
			        		}
			        	}
			        	if(moved){
			        		int movedCount = movedMethods.get(method);
			        		if(movedCount == 1) tempM += movedCount + " Move Method Refactor";
							else if(movedCount > 1) tempM += movedCount + " Move Method Refactors";
			        	}
			        	tempM += "\n";
			        } else {
			        	validExtractClassEntities++;
			        }
			        //methodIterator.remove(); // avoids a ConcurrentModificationException
			    }
			    
			    
			    Iterator fieldIterator = allRefactoredFields.entrySet().iterator();
			    while(fieldIterator.hasNext()){
			    	Map.Entry<String, Integer> fieldPair = (Map.Entry)fieldIterator.next();
			        String field = fieldPair.getKey();
			        Integer fieldCount = fieldPair.getValue();
			        if(fieldCount > 1){
				        //We only have one form of manipulating fields at the moment
				        tempF += field;
				        if(fieldCount == 1) tempM += fieldCount + " Extract Class Refactor";
						else if(fieldCount > 1) tempM += fieldCount + " Extract Class Refactors";
				        tempF += "\n";
			        }
			    	//fieldIterator.remove();
			    }

				//update the tooltip
			    String sourceClassToolTip = sourceClass.getName() + "\n\n";
			    sourceClassToolTip += "Conflicting Refactors:\n";
			    sourceClassToolTip += tempM;
			    sourceClassToolTip += tempF;
			    sourceClassToolTip += "\n(Left-click to print to console.)\n====================";
			    sourceClassFigure.setToolTip(new Label(sourceClassToolTip));
			    
			    
				//draw it
				if(!drawnClasses.contains(sourceClass)){
					classCoordinates.put(sourceClass, new int[]{curGridX,curGridY});
					//TODO update the tooltip to the source classes' conflicts
					final String sourceClassToolTipFinal = sourceClassToolTip;
				    sourceClassFigure.getLabel().addMouseListener(new MouseListener() {
						public void mouseReleased(MouseEvent arg0) {}
						public void mousePressed(MouseEvent arg0) {
							System.out.println(sourceClassToolTipFinal);
						}
						public void mouseDoubleClicked(MouseEvent arg0) {}
					});
					primary.add(sourceClassFigure, getNewClassRectangle());
					drawnClasses.add(sourceClass);
				}
				
				
				//draw supporting classes & connect them
				if(godClassRefactoringsMap.containsKey(sourceClass)){
					//create the extracted class
					String extractedClasslabel;
					HashSet<GodClassVisualizationData> candidates = godClassRefactoringsMap.get(sourceClass);
					int numOfRefactorings = candidates.size();
			        if(numOfRefactorings == 1) extractedClasslabel = "Extracted Class";
			        else extractedClasslabel = numOfRefactorings + " Extracted Classes";
			        ClassFigure extractedClassFigure = new ClassFigure(extractedClasslabel, DecorationConstants.classColor);
			        extractedClassFigure.setToolTip(new Label(extractedClasslabel));
			        
					int extractClassX = curGridX;
					
					primary.add(extractedClassFigure, getNewClassRectangle());
			        
			        String label = validExtractClassEntities + "/" + (validExtractClassEntities+invalidExtractClassEntities);
			        String extractClassToolTip = "Conflicting Refactors:\n";
			        extractClassToolTip += tempM;
			        extractClassToolTip += tempF;
			        extractClassToolTip += "\n(Left-click to print to console.)\n====================";
			        int sourceClassX = classCoordinates.get(sourceClass)[0];
			        JConnection connection;
			        if(sourceClassX < extractClassX){
			        	connection = sourceClassFigure.addRightLeftConnection(ConnectionType.METHOD_CALL_SOURCE, extractedClassFigure, label, extractClassToolTip);
			        } else if (sourceClassX > extractClassX){
			        	connection = sourceClassFigure.addLeftRightConnection(ConnectionType.READ_FIELD_TARGET, extractedClassFigure, label, extractClassToolTip);
			        } else {
			        	connection = sourceClassFigure.addRightRightConnection(ConnectionType.READ_FIELD_TARGET, extractedClassFigure, label, extractClassToolTip, classWidth-classWidth/2/2);
			        }
			        setConnectionColor(validExtractClassEntities, invalidExtractClassEntities, connection);
					connections.add(connection);
					
				}
				
				//Create the feature envy classes/connections
				if(featureEnvyClassRefactoringsMap.containsKey(sourceClass)){
					//for each candidate in the source class
					HashSet<FeatureEnvyVisualizationData> candidates = featureEnvyClassRefactoringsMap.get(sourceClass);
					HashSet<ClassObject> sourceTargetClasses = new HashSet<ClassObject>(); //since we need to compile methods per target, we don't do duplicate targets
					for( FeatureEnvyVisualizationData c1 :  candidates){
						
						HashSet<String> methods = new HashSet<String>();
						ClassObject targetClass = c1.getTargetClass();
						if(!sourceTargetClasses.contains(targetClass)){
							sourceTargetClasses.add(targetClass);
							//this is to avoid duplication
							ClassFigure targetClassFigure;
							if(classFigureMap.containsKey(targetClass)){
								targetClassFigure = classFigureMap.get(targetClass);
							} else if (featureEnvyTargetClassFigures.containsKey(targetClass)){
								targetClassFigure = featureEnvyTargetClassFigures.get(targetClass);
							} else {
								targetClassFigure = new ClassFigure(targetClass.getClassName(), DecorationConstants.classColor);
								targetClassFigure.setToolTip(new Label(targetClass.getName()));
							}
							
							//draw the target class if it hasn't already
							if(!drawnClasses.contains(targetClass)){
	
								if(!featureEnvyTargetClassFigures.containsKey(targetClass)) featureEnvyTargetClassFigures.put(targetClass, targetClassFigure);
								//Get all refactors that go to target class
								
								classCoordinates.put(targetClass, new int[]{curGridX,curGridY});
								primary.add(targetClassFigure, getNewClassRectangle());
								drawnClasses.add(targetClass);
							} else {
								//the target class has already been drawn
							}
							
							//we need to compile a list of all methods going from our source to a given target so we can make a single connection
							for(FeatureEnvyVisualizationData c2 :  candidates){
								if(c2.getTargetClass().equals(targetClass)){
									methods.add(c2.getMethodToBeMoved().getSignature());
								}
							}
							
							
							String moveMethodToolTip = "Conflicting Refactors:\n";
							int validMoveMethods = 0;
							for(String method : methods){
								int count = allRefactoredMethods.get(method);
								if(count > 1){
									moveMethodToolTip += method + ": ";
						        	boolean extracted = extractedMethods.containsKey(method);
						        	boolean moved = movedMethods.containsKey(method);
						        	if(extracted){
						        		int extractCount = extractedMethods.get(method);
						        		if(extractCount == 1) moveMethodToolTip += extractCount + " Extract Class Refactor";
										else if(extractCount > 1) moveMethodToolTip += extractCount + " Extract Class Refactors";
						        		if(moved){
						        			moveMethodToolTip += ", ";
						        		}
						        	}
						        	if(moved){
						        		int movedCount = movedMethods.get(method);
						        		if(movedCount == 1) moveMethodToolTip += movedCount + " Move Method Refactor";
										else if(movedCount > 1) moveMethodToolTip += movedCount + " Move Method Refactors";
						        	}
						        	moveMethodToolTip += "\n";
								} else {
									validMoveMethods++;
								}
							}
							
							moveMethodToolTip += "\n(Left-click to print to console.)\n====================";
							
							String label = validMoveMethods + "/" + methods.size(); 
							
							int sourceClassX = classCoordinates.get(sourceClass)[0];
							int targetClassX = classCoordinates.get(targetClass)[0];
					        JConnection connection;
					        if(sourceClassX < targetClassX){
					        	connection = sourceClassFigure.addRightLeftConnection(ConnectionType.READ_FIELD_TARGET, targetClassFigure, label, moveMethodToolTip);
					        } else if (sourceClassX > targetClassX){
					        	connection = sourceClassFigure.addLeftRightConnection(ConnectionType.READ_FIELD_TARGET, targetClassFigure, label, moveMethodToolTip);
					        } else {
					        	connection = sourceClassFigure.addRightRightConnection(ConnectionType.READ_FIELD_TARGET, targetClassFigure, label, moveMethodToolTip, classWidth-classWidth/2/2);
					        }
					        setConnectionColor(validMoveMethods, methods.size()-validMoveMethods, connection);
							connections.add(connection);
						}
						
			
					}
					
				}
				//sourceIterator.remove();
			}
		
			
		    root.add(connections, "Connections");
		}
	}
	
	private void setConnectionColor(int validEntities, int invalidEntities,
			JConnection connection) {
		if(invalidEntities == 0){
	        connection.setMethodToMethodStyle();
		} else if (validEntities <= (validEntities+invalidEntities)*.25){
			connection.setWriteStyle();
		} else connection.setReadStyle();
	}

	Rectangle getNewClassRectangle(){
		int x = startGridX + (curGridX * (classWidth + xGap));
		int y = startGridY + (curGridY * yGap);
		
		curGridX++;
		if(curGridX > maxGridX){
			curGridX = 0;
			curGridY++;
		}
		
		return new Rectangle(x, y, classWidth, -1);
	}

	public ScalableFreeformLayeredPane getRoot() {
		// TODO Auto-generated method stub
		return root;
	}

	
}
