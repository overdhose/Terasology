/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.miniion.gui;

import javax.vecmath.Vector2f;

import org.newdawn.slick.Color;
import org.terasology.entitySystem.In;
import org.terasology.game.Timer;
import org.terasology.miniion.components.MiniionSettingsComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ClickListener;
import org.terasology.rendering.gui.widgets.UILabel;
import org.terasology.rendering.gui.widgets.UIWindow;

public class UIScreenSettings extends UIWindow{
	
	@In
    private Timer timer;
	
	private final UILabel lblTitle, lblselover, lblWarning, lblExtraInfo;
	private final UIModButtonMenu btnOverlay, btnExtraInfo;
	
    public UIScreenSettings() {
    	lblTitle = new UILabel();
    	lblTitle.setText("Settings");
		lblTitle.setTextShadow(true);
		//lblstatTitle.setBorderSolid(new Vector4f(4f, 4f, 4f, 4f), Color.red);
		lblTitle.setPosition(new Vector2f(150 - (lblTitle.getSize().x /2), 10));
		//lblstatTitle.setPosition(new Vector2f(120, 10));
		lblTitle.setVisible(true);
		lblTitle.setColor(Color.green);
		addDisplayElement(lblTitle);
		
		lblExtraInfo = new UILabel();
		lblExtraInfo.setText("Show extra info");
		//lblExtraInfo.setWrap(true);
		//lblExtraInfo.setSize(new Vector2f(280, 30));
		lblExtraInfo.setPosition(new Vector2f(10, 30));
		lblExtraInfo.setVisible(true);
		lblExtraInfo.setColor(Color.green);
		addDisplayElement(lblExtraInfo);
		
		btnExtraInfo = new UIModButtonMenu(new Vector2f(50,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnExtraInfo.setPosition(new Vector2f(240, 30));
		btnExtraInfo.setId("extr");		
		btnExtraInfo.addClickListener(new ClickListener() {
			
			@Override
			public void click(UIDisplayElement element, int button) {
				if(MinionSystem.getSettings() != null){
					MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
					settingcomp.showExtraInfo = !settingcomp.showExtraInfo;
				}
				refreshScreen();
			}
		});
		btnExtraInfo.setVisible(true);
		addDisplayElement(btnExtraInfo);
		
		lblWarning = new UILabel();
		lblWarning.setText("Warning!!! activating these settings might cause serious performance drops. Use with care");
		lblWarning.setWrap(true);
		lblWarning.setSize(new Vector2f(280, 30));
		lblWarning.setPosition(new Vector2f(10, 300));
		lblWarning.setVisible(true);
		lblWarning.setColor(Color.red);
		addDisplayElement(lblWarning);
		
		lblselover = new UILabel();
		lblselover.setText("Selection overlay : renders selection boxes around new zones");
		lblselover.setWrap(true);
		lblselover.setSize(new Vector2f(280, 30));
		lblselover.setPosition(new Vector2f(10, 400));
		lblselover.setVisible(true);
		lblselover.setColor(Color.green);
		addDisplayElement(lblselover);
		
		btnOverlay = new UIModButtonMenu(new Vector2f(50,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnOverlay.setPosition(new Vector2f(125, 430));
		btnOverlay.setId("over");		
		btnOverlay.addClickListener(new ClickListener() {
			
			@Override
			public void click(UIDisplayElement element, int button) {
				if(MinionSystem.getSettings() != null){
					MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
					settingcomp.showSelection = !settingcomp.showSelection;
				}
				refreshScreen();
			}
		});
		btnOverlay.setVisible(true);
		addDisplayElement(btnOverlay);
		
		refreshScreen();
    }
    
    public void refreshScreen(){
    	if(MinionSystem.getSettings() != null){
    		MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
	    	if(settingcomp.showSelection){
				btnOverlay.setLabel("On");
	    	}else{
	    		btnOverlay.setLabel("Off");
	    	}
	    	if(settingcomp.showExtraInfo){
				btnExtraInfo.setLabel("On");
	    	}else{
	    		btnExtraInfo.setLabel("Off");
	    	}
    	}
    }
    
    @Override
    public void open() {
    	super.open();
    	MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
    	if(MinionSystem.getSettings() != null){
			if(settingcomp.showExtraInfo){
				btnExtraInfo.setToggleState(true);
			}else{
				btnExtraInfo.setToggleState(false);
			}
    	}
    	if(settingcomp.showSelection){
			btnOverlay.setToggleState(true);
    	}else{
    		btnOverlay.setToggleState(false);
    	}
    	refreshScreen();
    }
}
