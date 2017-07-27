package com.lostagain.nl.GWTish;

import java.util.ArrayList;
import java.util.logging.Logger;

import com.badlogic.gdx.graphics.Color;

/**
 * Crudely emulates some aspects of gwts htmlpanel widget
 * 
 * A htmlpanel is a panel that contains text, and which can attach child widgets to identified elements within that text.
 * As we have no real browser engine, dont expect real html support.
 * However, positioning elements by ID will be done the same.
 *  <div id="(elementIDToInsertObjectAt)"></div>
 * 
 * This class is based of a extension of FlowPanel. You can consider it a flowpanel with Label objects being thte automatic default contents
 * 
 * Note;
 * 		getElement().setInnerHTML(newText);		
 * will not work
 * 
 * setInnerText
 * addInnerText
 * 
 * instead
 * 
 * @author darkflame
 *
 */		

//TODO: we might need a option to split all labels by their newlines if this widget might resize
//this should only be ondemand though, else there might be a lot of wasted objects
public class InsertsPanel extends FlowPanel {
	public static Logger Log = Logger.getLogger("Gwtish.InsertsPanel"); 
	Label currentActiveLabel;
	//[[oldlab][randomobject][oldlab][currentlabel...]]



	public InsertsPanel() {
		super();
		currentActiveLabel=getDefaultLabelType("");
		super.add(currentActiveLabel);
	}





	public InsertsPanel(float maxWidth) {
		super(maxWidth);

		currentActiveLabel=getDefaultLabelType("");
		super.add(currentActiveLabel);
	}


	private Label getDefaultLabelType(String contents) {
		Label label = new Label(contents);
		label.setInterpretBRasNewLine(true); //as this sort of emulates html

		label.setMaxWidth(super.maxWidth); //ensures never wider then this container. However, inserts may still make textlabels go outside the boundary right now

		return label;
	}


	public void addInnerText(String addThisText){
		currentActiveLabel.addText(addThisText);

	}

	public void setInnerText(String addThisText){
		super.clear();
		super.add(currentActiveLabel);
		currentActiveLabel.setText(addThisText);

	}


	public void add(Widget newwidget,String id){
		//search all elements
		//look in Labels for id match
		//if found...
		//create labelbefore
		//create labelafter
		//replace

		String IDstring = "<div id=\""+id+"\"></div>";

		ArrayList<Widget> arraycopy= new ArrayList<>(super.contents);

		for (Widget widget : arraycopy) {

			if (widget instanceof Label){

				Log.info("testing label");
				Label lab = (Label)widget;
				String labtext=lab.getText();

				int insertstart = labtext.indexOf(IDstring);

				if (insertstart>-1){

					Log.info("testing label: match found within:"+labtext);
					String before = labtext.substring(0, insertstart);
					String after = labtext.substring(insertstart+IDstring.length());

					Log.info(before+"[widget]"+after);


					int index=contents.indexOf(lab)+1; //location of current label to replace

					Log.info("[index]"+index);
					Label afterlab = null;
					if (!after.isEmpty()){ 						//we dont make empty labels
						afterlab = getDefaultLabelType(after);						
						super.insert(afterlab, index); //inserts it before
					}
					super.insert(newwidget, index); //inserts it before	

					if (!before.isEmpty()){						//we dont make empty labels



						Label beforelab = getDefaultLabelType(before);	
						super.insert(beforelab, index); //inserts it before

						splitbylastnewline(beforelab);	//see below
					}

					super.remove(lab); //remove old
					lab.dispose();

					Log.info("[count:]"+getWidgetCount());

					//split after as well
					//a) use splitbynextnewline if theres already a widget after
					//b) flag to use it next time if not
					if (afterlab!=null){
						splitbynextnewline(afterlab);
					} else {

					}

					//	TODO: somehow batch add? insert does a lot of redundant work

					//TODO: we also need to split by whenever the previous newline was, this is too ensure the text is devided squarely
					//
					//[[oldlab.........\n
					//.................\n
					//.................\n
					//......][divid]
					//
					//convert too:
					//[[oldlab.........\n
					//.................\n
					//.................]
					//[lab2.][divid]
					//
					//And split next text too!
					//
					//we need some wy in flowpanel to force newlines too? a dummy newline widget?
				}

			}

		}


		Log.info("debug:\n"+getWidgetDebugString());
		Widget lastwidget = contents.get(contents.size()-1);

		//currentActiveLabel should always be the last label, ensuring there is one first
		if (lastwidget instanceof Label){			
			currentActiveLabel=(Label) lastwidget;
		} else {
			Label newcurrentlabel = this.getDefaultLabelType("");
			currentActiveLabel=newcurrentlabel;
			super.add(newcurrentlabel);

		}


	}




	private void splitbylastnewline(Label lab) {
		String labtext=lab.getText().toLowerCase();

		boolean countbr = lab.isInterpretBRasNewLine();

		//return if no newlines
		if (countbr){
			//if it contains no nlss && no brs
			if (!labtext.contains("\n") && !labtext.contains("<br>"))
			{
				Log.info("no nextnl or br ");
				return ;
			}			
		} else {
			//if it contains no nlss 
			if (!labtext.contains("\n") )
			{
				Log.info("no nextnl ");
				return ;
			}
		}
		//-------------

		// loc of last newline
		int lastnl = labtext.lastIndexOf("\n");
		String newlinetype="\n";
		if (countbr){
			int lastnlbr = labtext.lastIndexOf("<br>");
			if (lastnlbr>lastnl){
				lastnl=lastnlbr;
				newlinetype="<br>";
			}
		}


		Log.info("lastnl:\n"+lastnl);
		int newlineendloc = lastnl+newlinetype.length();

		insertNewLinebetween(lab, lastnl, newlineendloc);


		return ;
	}



	private void splitbynextnewline(Label lab) {


		String labtext=lab.getText().toLowerCase();

		Log.info("splitbynextnewline:\n"+labtext);
		boolean countbr = lab.isInterpretBRasNewLine();



		//return if no newlines
		if (countbr){
			//if it contains no nlss && no brs
			if (!labtext.contains("\n") && !labtext.contains("<br>"))
			{
				Log.info("no nextnl or br ");
				return ;
			}			
		} else {
			//if it contains no nlss 
			if (!labtext.contains("\n") )
			{
				Log.info("no nextnl ");
				return ;
			}
		}
		//-------------

		// loc of last newline
		int next_nl = labtext.indexOf("\n");
		String newlinetype="\n";
		if (countbr){
			int lastnlbr = labtext.indexOf("<br>");
			if ((lastnlbr<next_nl) && (lastnlbr!=-1)){
				next_nl=lastnlbr;
				newlinetype="<br>";
			}
		}


		Log.info("nextnl:\n"+next_nl);
		int newlineendloc = next_nl+newlinetype.length();

		insertNewLinebetween(lab, next_nl, newlineendloc);


		return ;
	}








	private void insertNewLinebetween(Label lab, int newlinestartloc, int newlineendloc) {
		String lab_text=lab.getText();		
		String before = lab_text.substring(0, newlinestartloc);
		String after = lab_text.substring(newlineendloc);

		Log.info(before+"[nl]"+after);

		NewLinePlacer testnewline = new NewLinePlacer();
		testnewline.setMinSize(15, 15); //to help tests make it visible
		testnewline.getStyle().setBackgroundColor(Color.RED);
		int index=contents.indexOf(lab)+1; //location of current label to replace


		if (!after.isEmpty()){ 						//we dont make empty labels
			Label afterlab = getDefaultLabelType(after);						
			super.insert(afterlab, index); //inserts it before
		}
		super.insert(testnewline, index); //inserts it before	

		if (!before.isEmpty()){						//we dont make empty labels
			Label beforelab = getDefaultLabelType(before);	
			super.insert(beforelab, index); //inserts it before
		}

		super.remove(lab); //remove old
		lab.dispose();
	}




}
