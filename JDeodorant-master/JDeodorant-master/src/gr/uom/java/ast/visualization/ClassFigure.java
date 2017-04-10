package gr.uom.java.ast.visualization;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.CompoundBorder;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.swt.graphics.Color;


public class ClassFigure extends Figure {
	private CompartmentFigure fieldFigure = new CompartmentFigure();
	private CompartmentFigure methodFigure = new CompartmentFigure();
	private CompartmentFigure extractMethodFigure = new CompartmentFigure();
	private SectionCompartment methodSectionCompartment ;
	private SectionCompartment fieldSectionCompartment = new SectionCompartment(3) ;
	
	public ClassFigure(String name, Color color) {
		ToolbarLayout layout = new ToolbarLayout();
		layout.setSpacing(5);
		setLayoutManager(layout);	
		setBorder(new CompoundBorder( new LineBorder(1), new MarginBorder(0, 0, 0, 0)));
		setBackgroundColor(color);
		setOpaque(true);

		Label className = new Label(name, DecorationConstants.CLASS);
		className.setToolTip(new Label(name));
		className.setFont(DecorationConstants.classFont);

		add(className);

		new ClassFigureMover(this);

	}

	public void addThreeCompartments(){
		addFieldCompartment();
		extractMethodFigure.setBorder(new LineBorder());
		extractMethodFigure.setBackgroundColor(DecorationConstants.entityColor);
		add(extractMethodFigure);
		add(methodFigure);
	}

	public void addTwoCompartments(){
		this.addFieldCompartment();
		methodFigure.setBorder(new CompartmentFigureBorder());
		add(methodFigure);
	}
	
	public void addFieldCompartment(){
		fieldFigure.setBorder(new CompartmentFigureBorder());
		add(fieldFigure);
	}
	
	public void addMethodCompartment(){
		methodFigure.setBorder(new CompartmentFigureBorder());
		add(methodFigure);
	}

	public void addMethodSectionCompartment(int columns){
		methodSectionCompartment = new SectionCompartment(columns);
		add(methodSectionCompartment);
	}

	public void addFieldSectionCompartment(){
		add(fieldSectionCompartment);
	}

	public SectionCompartment getFieldSectionCompartment() {
		return fieldSectionCompartment;
	}


	public SectionCompartment getMethodSectionCompartment() {
		return methodSectionCompartment;
	}



	public CompartmentFigure getFieldsCompartment() {
		return fieldFigure;
	}
	public CompartmentFigure getMethodsCompartment() {
		return methodFigure;
	}

	public CompartmentFigure getExtractMethodCompartment() {
		return extractMethodFigure;
	}
	
	/////////
	
	private String name;
	private List<JConnection> outgoingConnections = new ArrayList<JConnection>();
	private LeftAnchor leftAnchor= null;
	private RightAnchor rightAnchor= null;
	
	public LeftAnchor getLeftAnchor() {
		return leftAnchor;
	}

	public void setLeftAnchor(LeftAnchor leftAnchor) {
		this.leftAnchor = leftAnchor;
	}

	public RightAnchor getRightAnchor() {
		return rightAnchor;
	}

	public void setRightAnchor(RightAnchor rightAnchor) {
		this.rightAnchor = rightAnchor;
	}

	public String getName() {
		return name;
	}

	public List<JConnection> getOutgoingConnections() {
		return outgoingConnections;
	}
	
	public JConnection addLeftLeftConnection(ConnectionType type, ClassFigure label, Integer occurences, int bendHeight){

		JConnection connection = new JConnection(type);

		connection.setLeftLeftAnchors(this, label);

		//connection.setSourceBendRouter(bendHeight, classWidth);
		connection.setFullBendRouter(bendHeight);
		connection.setLabel(occurences);
		outgoingConnections.add(connection);


		return connection;
	}

	public JConnection addRightRightConnection(ConnectionType type, ClassFigure label, Integer occurences, int bendHeight){

		JConnection connection = new JConnection(type);

		connection.setRightRightAnchors(this, label);

		//connection.setSourceBendRouter(-bendHeight, -classWidth);
		connection.setFullBendRouter(-bendHeight);
		connection.setLabel(occurences);
		outgoingConnections.add(connection);


		return connection;
	}

	public JConnection addRightLeftConnection(ConnectionType type, ClassFigure label, Integer occurences){

		JConnection connection = new JConnection(type);
		connection.setRightLeftAnchors(this, label);
		connection.setLabel(occurences);

		outgoingConnections.add(connection);
		return connection;
	}

	public JConnection addLeftRightConnection(ConnectionType type,ClassFigure label, Integer occurences){

		JConnection connection = new JConnection(type);

		connection.setLeftRightAnchors(this, label);

		connection.setLabel(occurences);
		outgoingConnections.add(connection);
		return connection;
	}

}