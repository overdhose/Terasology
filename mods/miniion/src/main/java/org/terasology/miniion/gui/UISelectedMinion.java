package org.terasology.miniion.gui;

import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glTranslatef;

import javax.vecmath.Vector2f;

import org.lwjgl.opengl.GL11;
import org.newdawn.slick.Color;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.model.inventory.Icon;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.miniion.gui.UIModButton;
import org.terasology.miniion.gui.UIModButton.ButtonType;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.rendering.gui.widgets.UICompositeScrollable;
import org.terasology.rendering.gui.widgets.UILabel;

public class UISelectedMinion extends UICompositeScrollable{
	
	private class UIMinionbarCell extends UIDisplayContainer {     

        private EntityRef minion;               
        
        public UIMinionbarCell() {
            setSize(new Vector2f(48f, 48f));                        
            layout();
        }
        
        public void clearMinion(){
        	this.minion = null;
        	lblBehaviour.setVisible(false);
    		butfollow.setVisible(false);
    		butInventory.setVisible(false);
    		butBye.setVisible(false);
    		butStay.setVisible(false);
    		butAttack.setVisible(false);
    		butGather.setVisible(false);
    		lblname.setVisible(false);
    		lblflavor.setVisible(false);
    		setBehaviourToggle(null);
        }               
        
        @Override
        public void update() {          
        }
        
    	@Override
    	public void layout() {

    	}

        @Override
        public void render() {
        	if(minion != null)
        	{
        		if(this.minion.hasComponent(MinionComponent.class))
        		{
		            if (minion.getComponent(MinionComponent.class).icon.isEmpty()) {
		                Icon icon = Icon.get("minionskull");
		                if (icon != null) {
		                    renderIcon(icon);
		                }
		            } else {
		                Icon icon = Icon.get(minion.getComponent(MinionComponent.class).icon);
		                if (icon != null) {
		                    renderIcon(icon);
		                }
		            }
        		}
        	}
        }

        private void renderIcon(Icon icon) {
            glEnable(GL11.GL_DEPTH_TEST);
            glClear(GL11.GL_DEPTH_BUFFER_BIT);
            glPushMatrix();
            glTranslatef(20f, 20f, 0f);
            icon.render();
            glPopMatrix();
            glDisable(GL11.GL_DEPTH_TEST);
        }
        
        public void setMinion(EntityRef minion){
        	this.minion = minion;
        }
    }
	
	private UIMinionbarCell cell = new UIMinionbarCell();
	private UIScreenBookOreo minionscreen;
	
	private UIModButton butfollow, butStay, butInventory, butBye, butAttack, butGather;
	private UILabel lblBehaviour, lblname, lblflavor;
	
	public UISelectedMinion(UIScreenBookOreo minionscreen){
		
		this.minionscreen = minionscreen;
		
		cell.setPosition(new Vector2f(0, 30));
		cell.setVisible(true);		
		this.addDisplayElement(cell);
		
		lblname = new UILabel("");
		lblname.setPosition(new Vector2f(50, 30));
		lblname.setColor(Color.black);
		lblname.setVisible(false);
		this.addDisplayElement(lblname);
		
		lblflavor = new UILabel("");
		lblflavor.setPosition(new Vector2f(50, 50));
		lblflavor.setColor(Color.black);
		lblflavor.setVisible(false);
		this.addDisplayElement(lblflavor);
		
		lblBehaviour = new UILabel("Behaviour");
		lblBehaviour.setPosition(new Vector2f(20, 80));
		lblBehaviour.setColor(Color.black);
		lblBehaviour.setVisible(false);
		this.addDisplayElement(lblBehaviour);
		
		butfollow = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butfollow.setLabel("Follow");
		butfollow.setColorOffset(180);
		butfollow.setVisible(false);
		butfollow.setPosition(new Vector2f(20, 110));
		butfollow.setId("btnFollow");
		butfollow.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
            	executeClick(element,button);
            }
        });
		this.addDisplayElement(butfollow);
		
		butStay = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butStay.setLabel("Stay");
		butStay.setColorOffset(180);
		butStay.setVisible(false);
		butStay.setPosition(new Vector2f(80, 110));
		butStay.setId("btnStay");
		butStay.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
            	executeClick(element,button);
            }
        });
		this.addDisplayElement(butStay);
		
		butAttack = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butAttack.setLabel("Attack");
		butAttack.setColorOffset(180);
		butAttack.setVisible(false);
		butAttack.setPosition(new Vector2f(140, 110));
		butAttack.setId("btnAttack");
		butAttack.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
            	executeClick(element,button);
            }
        });
		this.addDisplayElement(butAttack);
		
		butGather = new UIModButton(new Vector2f(50, 20), ButtonType.TOGGLE);
		butGather.setLabel("Gather");
		butGather.setColorOffset(180);
		butGather.setVisible(false);
		butGather.setPosition(new Vector2f(200, 110));
		butGather.setId("btnGather");
		butGather.addClickListener(new ClickListener() {
            @Override
            public void click(UIDisplayElement element, int button) {
            	executeClick(element,button);
            }
        });
		this.addDisplayElement(butGather);
		
		butInventory = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butInventory.setLabel("Inventory");
		butInventory.setVisible(false);
		butInventory.setPosition(new Vector2f(20, 170));
		butInventory.addClickListener(new ClickListener() {
			@Override
            public void click(UIDisplayElement element, int button) {
				openInventory();
            }
        });
		this.addDisplayElement(butInventory);
		
		butBye = new UIModButton(new Vector2f(100, 20), ButtonType.NORMAL);
		butBye.setLabel("bye bye");
		butBye.setColorOffset(120);
		butBye.setVisible(false);
		butBye.setPosition(new Vector2f(140, 200));
		butBye.addClickListener(new ClickListener() {
			@Override
            public void click(UIDisplayElement element, int button) {
				destroyMinion();
            }
        });
		this.addDisplayElement(butBye);
		
	}
	
	public void setMinion(EntityRef minion){
		MinionSystem.setActiveMinion(minion);
		cell.setMinion(minion);		
		MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
		lblname.setText(minioncomp.name);
		lblflavor.setText(minioncomp.flavortext);
		lblname.setVisible(true);
		lblflavor.setVisible(true);
		lblBehaviour.setVisible(true);
		butfollow.setVisible(true);
		butInventory.setVisible(true);
		butBye.setVisible(true);
		butStay.setVisible(true);
		butAttack.setVisible(true);
		butGather.setVisible(true);
		lblname.setVisible(true);
		lblflavor.setVisible(true);
		if(minioncomp.minionBehaviour == MinionBehaviour.Follow){
			setBehaviourToggle(butfollow);
		}
		else if(minioncomp.minionBehaviour == MinionBehaviour.Stay){
			setBehaviourToggle(butStay);
		}
		else if(minioncomp.minionBehaviour == MinionBehaviour.Attack){
			setBehaviourToggle(butAttack);
		}
		else if(minioncomp.minionBehaviour == MinionBehaviour.Gather){
			setBehaviourToggle(butGather);
		}
		else {
			setBehaviourToggle(null);
		}		
	}
	
	public void executeClick(UIDisplayElement element, int id){
		UIModButton clickedbutton = (UIModButton)element;
		MinionComponent minioncomp = this.cell.minion.getComponent(MinionComponent.class);
		if(clickedbutton.getId() == "btnStay")
		{
			minioncomp.minionBehaviour = MinionBehaviour.Stay;
			setBehaviourToggle(butStay);
		}
		if(clickedbutton.getId() == "btnFollow")
		{
			minioncomp.minionBehaviour = MinionBehaviour.Follow;
			setBehaviourToggle(butfollow);
		}
		if(clickedbutton.getId() == "btnAttack")
		{
			minioncomp.minionBehaviour = MinionBehaviour.Attack;
			setBehaviourToggle(butAttack);
		}
		if(clickedbutton.getId() == "btnGather")
		{
			minioncomp.minionBehaviour = MinionBehaviour.Gather;
			setBehaviourToggle(butGather);
		}
		this.cell.minion.saveComponent(minioncomp);
	}
	
	 private void setBehaviourToggle(UIModButton button)
     {
     	butStay.setToggleState(false);
     	butfollow.setToggleState(false);
     	butAttack.setToggleState(false);
     	butGather.setToggleState(false);
     	if(button != null){
     		button.setToggleState(true);
     	}
     }
	
	private void destroyMinion(){
		if(this.cell.minion != null){
			MinionComponent minioncomp = this.cell.minion.getComponent(MinionComponent.class);
			minioncomp.minionBehaviour = MinionBehaviour.Die;
			this.cell.minion.saveComponent(minioncomp);
			this.cell.clearMinion();
			if(minionscreen != null){
				minionscreen.refresh();
			}
		}
	}
	
	private void openInventory(){
		if(this.cell.minion != null){
			this.cell.minion.send(new ActivateEvent(this.cell.minion, CoreRegistry.get(LocalPlayer.class).getEntity()));
			this.cell.minion.getComponent(MinionComponent.class).minionBehaviour = MinionBehaviour.Stay;
			this.cell.minion.saveComponent(this.cell.minion.getComponent(MinionComponent.class));
			this.getGUIManager().closeWindow(minionscreen);
		}
	}
}