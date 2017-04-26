package gr.uom.java.jdeodorant.refactoring.views;

import gr.uom.java.ast.ClassObject;
import gr.uom.java.ast.ConstructorObject;
import gr.uom.java.ast.FieldObject;
import gr.uom.java.ast.MethodObject;
import gr.uom.java.ast.TypeObject;
import gr.uom.java.ast.visualization.FeatureEnvyDiagram;
import gr.uom.java.ast.visualization.FeatureEnvyVisualizationData;
import gr.uom.java.ast.visualization.GodClassDiagram2;
import gr.uom.java.ast.visualization.GodClassVisualizationData;
import gr.uom.java.ast.visualization.RefactoringDiagram;
import gr.uom.java.ast.visualization.ZoomInputAction;
import gr.uom.java.ast.visualization.VisualizationData;
import gr.uom.java.ast.visualization.ZoomAction;
import gr.uom.java.jdeodorant.refactoring.Activator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.ScalableFreeformLayeredPane;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ltk.core.refactoring.RefactoringDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class CodeSmellVisualization extends ViewPart {

	public static final String ID = "gr.uom.java.jdeodorant.views.CodeSmellVisualization";
	private FigureCanvas figureCanvas; 
	private ScalableFreeformLayeredPane root = null;
	private boolean ctrlPressed= false;

	public void createPartControl(Composite parent) {

		parent.setLayout(new FillLayout());

		figureCanvas = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED);
		figureCanvas.setBackground(ColorConstants.white);

		VisualizationData data = CodeSmellVisualizationDataSingleton.getData();

		if(data != null || CodeSmellVisualizationDataSingleton.displayRefactoringDiagram) {
			if(CodeSmellVisualizationDataSingleton.displayRefactoringDiagram){
				CodeSmellVisualizationDataSingleton.displayRefactoringDiagram = false;
				RefactoringDiagram diagram = new RefactoringDiagram();
				root= diagram.getRoot();
			}
			else if(data instanceof GodClassVisualizationData) {
				//
				GodClassDiagram2 diagram = new GodClassDiagram2((GodClassVisualizationData)data);
				//RefactoringDiagram diagram = new RefactoringDiagram();
				root= diagram.getRoot();
			}
			else if(data instanceof FeatureEnvyVisualizationData) {
				FeatureEnvyDiagram diagram = new FeatureEnvyDiagram((FeatureEnvyVisualizationData)data);
				root= diagram.getRoot();
			}

			figureCanvas.setViewport(new FreeformViewport());


			figureCanvas.addKeyListener( new KeyListener() {

				public void keyPressed(KeyEvent e) {
					if(e.keyCode == SWT.CTRL){
						ctrlPressed = true;
					}
				}

				public void keyReleased(KeyEvent e) {
					if(e.keyCode== SWT.CTRL)
						ctrlPressed = false;

				}
			});

			MouseWheelListener listener = new MouseWheelListener() {
				private double scale;
				private static final double ZOOM_INCRENENT = 0.1;
				private static final double ZOOM_DECREMENT = 0.1;

				private void zoom(int count, Point point) {
					if (count > 0) {
						scale += ZOOM_INCRENENT;

					} else {
						scale -= ZOOM_DECREMENT;
					}

					if (scale <= 0) {
						scale = 0;
					}
					FreeformViewport viewport = (FreeformViewport) root.getParent();

					if(scale>1){
						viewport.setHorizontalLocation((int) (point.x*(scale -1)+ scale*viewport.getLocation().x));
						viewport.setVerticalLocation((int) (point.y*(scale-1)+scale*viewport.getLocation().y));
					}

					root.setScale(scale);
				}

				public void mouseScrolled(MouseEvent e) {

					if(ctrlPressed == true){
						scale = root.getScale();
						Point point = new Point(e.x,e.y);
						int count = e.count;
						zoom(count, point);

					}

				}
			};

			figureCanvas.addMouseWheelListener(listener);
			figureCanvas.setContents(root);
		}

		// Custom Action for the View's Menu  
		ImageDescriptor imageDescriptor = Activator.getImageDescriptor("/icons/" + "magnifier.png");
		IActionBars bars = getViewSite().getActionBars();
		IToolBarManager manager = bars.getToolBarManager();
		Action act=new Action("Zoom",SWT.DROP_DOWN){};
		act.setImageDescriptor(imageDescriptor);
		act.setMenuCreator(new MyMenuCreator());
		manager.add(act);
		
		
		
		
		///
		
		Action importRefactors = new Action("Import Refactorings") {
			public void run(){
		        Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				FileDialog dialog = new FileDialog(shell, SWT.OPEN);
				   dialog.setFilterExtensions(new String [] {"*.txt"});
				   //dialog.setFilterPath("c:\\temp");
				   String result = dialog.open();
				   
				   BufferedReader br = null;
			        String line = "";
			        String cvsSplitBy = "\t";

			        try {

			            br = new BufferedReader(new FileReader(result));
			            while ((line = br.readLine()) != null) {

			                // use comma as separator
			                String[] cells = line.split(cvsSplitBy);

			                if(cells != null){
			                	if(cells.length > 0){
					                if(cells[0].toLowerCase().equals("extractclass")) parseExtractClassRefactoring(cells);
					                else if(cells[0].toLowerCase().equals("movemethod")) parseMoveMethodRefactoring(cells);
			                	}
			                }

			            }

			        } catch (FileNotFoundException e) {
			            e.printStackTrace();
			        } catch (IOException e) {
			            e.printStackTrace();
			        } finally {
			            if (br != null) {
			                try {
			                    br.close();
			                } catch (IOException e) {
			                    e.printStackTrace();
			                }
			            }
			        }
				
			}

			private void parseMoveMethodRefactoring(String[] cells) {
				String[] names = cells[1].split(":");
				String sourceClass = names[0].replaceAll("\"", "");
				String methodString = names[2].split("\\(")[0];
				String returnType = names[3];
				
				FeatureEnvyVisualizationData featureEnvy = createFatureEnvyVisualizationData(sourceClass, cells[2], methodString);
				CodeSmellVisualizationDataSingleton.addFeatureEnvy(featureEnvy);
			}

			private void parseExtractClassRefactoring(String[] cells) {
				String sourceClass = cells[1];
				cells[2] = cells[2].replaceAll("\\[", "").replaceAll("\\]","");
				List<String> entities = split(cells[2]);
				
				HashSet<MethodObject> extractedMethods = new HashSet<MethodObject>();
				HashSet<FieldObject> extractedFields = new HashSet<FieldObject>();
				String className = null;
				for(String entity : entities){
					entity = entity.replaceAll("\"", "");
					
					if(entity.contains("(")){
						String[] names = entity.split(":");
						String methodName = names[2].split("\\(")[0];
						MethodObject method = createMethodObject(names[0], methodName, names[3]);
						className = names[0];
						extractedMethods.add(method);
					} else if(entity.contains(" ")){
						if(entity.substring(0, 1).equals(" ")){
							entity = entity.substring(1);
						}
						String[] names = entity.split(" ");
						String[] classAndType = names[0].split("::");
						if(classAndType.length < 2 || names.length < 2){
							int i = 0;
							int j = i;
						}
						FieldObject field = createFieldObject(classAndType[1], names[1], classAndType[0]);
						className = classAndType[0];
						extractedFields.add(field);
					}
					
				}
				GodClassVisualizationData godClass = createGodClassData(className, extractedMethods, extractedFields);
				CodeSmellVisualizationDataSingleton.addGodClass(godClass);
			};
			
			public List<String> split(String orig) {
			    List<String> splitted = new ArrayList<String>();
			    int nextingLevel = 0;
			    StringBuilder result = new StringBuilder();
			    for (char c : orig.toCharArray()) {
			        if (c == ',' && nextingLevel == 0) {
			            splitted.add(result.toString());
			            result.setLength(0);// clean buffer
			        } else {
			            if (c == '(')
			                nextingLevel++;
			            if (c == ')')
			                nextingLevel--;
			            result.append(c);
			        }
			    }
			    splitted.add(result.toString());
			    return splitted;
			}
		};
		importRefactors.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_OPEN_MARKER));
		manager.add(importRefactors);
		
	}
	
	public FieldObject createFieldObject(String type, String fieldName, String className){
		TypeObject typeObject = new TypeObject(type);
		FieldObject field = new FieldObject(typeObject, fieldName);
		field.setClassName(className);
		return field;
	}
	
	public MethodObject createMethodObject(String className, String methodName, String returnType){
		ConstructorObject co = new ConstructorObject();
		co.setClassName(className);
		co.setName(methodName);
		MethodObject method = new MethodObject(co);
		method.setReturnType(new TypeObject(returnType));
		return method;
	}
	
	
	
	public GodClassVisualizationData createGodClassData(String className, HashSet<MethodObject> extractedMethods, HashSet<FieldObject> extractedFields){
		ClassObject sourceClass = new ClassObject();
		sourceClass.setName(className);
		GodClassVisualizationData data = new GodClassVisualizationData(sourceClass, extractedMethods, extractedFields);
		return data;
	}
	
	public FeatureEnvyVisualizationData createFatureEnvyVisualizationData(String sourceClassName, String targetClassName, String methodName){
		ClassObject sourceClass = new ClassObject();
		sourceClass.setName(sourceClassName);
		ClassObject targetClass = new ClassObject();
		targetClass.setName(targetClassName);
		ConstructorObject co = new ConstructorObject();
		co.setClassName(sourceClassName);
		co.setName(methodName);
		MethodObject methodToBeMoved = new MethodObject(co);
		FeatureEnvyVisualizationData data = new FeatureEnvyVisualizationData(sourceClass, methodToBeMoved, targetClass);
		return data;
	}


	class MyMenuCreator implements IMenuCreator{

		private IAction action;
		private Menu menu;

		public void selectionChanged(IAction action, ISelection selection)
		{
			if (action != this.action)
			{
				action.setMenuCreator(this);
				this.action = action;
			}
		} 

		public Menu getMenu(Control ctrl){
			Menu menu = new Menu(ctrl);
			addActionToMenu(menu, newZoomAction(0.5));
			addActionToMenu(menu, newZoomAction(1));
			addActionToMenu(menu, newZoomAction(2));
			addActionToMenu(menu, newZoomAction(0));

			ZoomInputAction inputZoomAction = new ZoomInputAction(root);
			inputZoomAction.setText("Other...");

			addActionToMenu(menu, inputZoomAction);
			return menu;

		}

		public void dispose() {
			if (menu != null)
			{
				menu.dispose();
			}
		}

		public Menu getMenu(Menu parent) {
			return null;
		}

		private void addActionToMenu(Menu menu, IAction action)
		{
			ActionContributionItem item= new ActionContributionItem(action);
			item.fill(menu, -1);
		}
	}

	public void setFocus() {

	}

	public ZoomAction newZoomAction(double scale){
		ZoomAction zoomAction = new ZoomAction(root, scale);
		if(scale != 0){
			double percent = scale*100;
			zoomAction.setText((int) percent +"%");
			zoomAction.setImageDescriptor(Activator.getImageDescriptor("/icons/" + "magnifier.png"));
		}else
			zoomAction.setText("Scale to Fit");
		return zoomAction;
	}

}
