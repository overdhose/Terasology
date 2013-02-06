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

import java.util.List;
import java.util.Map.Entry;

import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;
import org.terasology.asset.Assets;
import org.terasology.entitySystem.EntityManager;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.LocalPlayer;
import org.terasology.miniion.components.MiniionSettingsComponent;
import org.terasology.miniion.components.MinionComponent;
import org.terasology.miniion.components.SimpleMinionAIComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.miniion.minionenum.MinionBehaviour;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.miniion.utilities.MinionRecipe;
import org.terasology.miniion.utilities.Zone;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.*;
import org.terasology.rendering.gui.layout.GridLayout;
import org.terasology.rendering.gui.widgets.*;

public class UIActiveMinion extends UIWindow{
	
	private final static int extrainfoheight = 60;
	
	private final UILabel lblname, lblflavor, lblzone, lblrecipe, lblMessage, lblHover;
	private final UIImage backgroundmain, backgroundextra;
	private final UIComposite behaviourlist, actionlist;
	private final UIList uiMainlist, uiDetailList;
	private final UIScreenStats uistats;
	private final UIScreenSettings uisettings;
	private final UIModButtonArrow btnLeft, btnRight, btnBehaviour, btnActions;
	//behaviour buttons
	private final UIModButtonMenu btnStay, btnFollow, btnAttack, btnGather, btnWork, btnTerra;
	//action buttons
	private final UIModButtonMenu btnInventory, btnZone, btnRecipe, btnClear, btnBye, btnStats;
	//special buttons
	private final UIModButtonSpecial btnMinionlist, btnSettings, btnZoneMan, btnMessage, btnCraftMan, btnBuildMan, btnEmpty;
	
	public UIActiveMinion() {
		setId("activeminiion");
		setModal(true);
		setCloseKeys(new int[] { Keyboard.KEY_ESCAPE });
		
		addMouseButtonListener(new MouseButtonListener() {

			@Override
			public void wheel(UIDisplayElement element, int wheel,
					boolean intersect) {

			}

			@Override
			public void up(UIDisplayElement element, int button,
					boolean intersect) {
				if (button == 1) {
					close();
				}
			}

			@Override
			public void down(UIDisplayElement element, int button,
					boolean intersect) {

			}
		});
		
		setSize(new Vector2f(new Vector2f(Display.getWidth(), Display.getHeight())));
		setVerticalAlign(EVerticalAlign.TOP);
		setHorizontalAlign(EHorizontalAlign.LEFT);
		setVisible(true);
		
		backgroundmain = new UIImage();
		backgroundmain.setTexture(Assets.getTexture("miniion:activeminionback"));
		backgroundmain.setSize(new Vector2f(300, 132));
		backgroundmain.setPosition(new Vector2f(this.getSize().x - 300, 0));
		backgroundmain.setVisible(true);
		addDisplayElement(backgroundmain);
		
		backgroundextra = new UIImage();
		backgroundextra.setTexture(Assets.getTexture("miniion:modularback"));
		backgroundextra.setSize(new Vector2f(300, extrainfoheight));
		backgroundextra.setPosition(new Vector2f(this.getSize().x - 600, 0));
		backgroundextra.setVisible(false);
		addDisplayElement(backgroundextra);
		
		lblHover = new UILabel();
		lblHover.setPosition(new Vector2f(15, 33));
		lblHover.setSize(new Vector2f(260,15));		
		lblHover.setVisible(true);
		backgroundmain.addDisplayElement(lblHover);
		
		lblname = new UILabel();
		lblname.setPosition(new Vector2f(45, 90));
		lblname.setSize(new Vector2f(260,15));		
		lblname.setVisible(true);
		backgroundmain.addDisplayElement(lblname);
		
		lblflavor = new UILabel();
		lblflavor.setPosition(new Vector2f(45, 55));
		lblflavor.setWrap(true);
		lblflavor.setSize(new Vector2f(250, 30));
		lblflavor.setVisible(true);
		backgroundmain.addDisplayElement(lblflavor);
		
		lblMessage = new UILabel();
		lblMessage.setPosition(new Vector2f(-100, 10));
		lblMessage.setWrap(true);
		lblMessage.setSize(new Vector2f(250, 60));
		lblMessage.setVisible(true);
		backgroundmain.addDisplayElement(lblMessage);
		
		lblzone = new UILabel();
		lblzone.setPosition(new Vector2f(5, 0));
		lblzone.setVisible(true);
		backgroundextra.addDisplayElement(lblzone);	
		
		lblrecipe = new UILabel();
		lblrecipe.setPosition(new Vector2f(5, 15));
		lblrecipe.setVisible(true);
		backgroundextra.addDisplayElement(lblrecipe);	
		
		btnLeft = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.LEFT, new Vector2f(10, -54), "previous miniion");
		btnLeft.setPosition(new Vector2f(5,87));
		btnLeft.setId("previousminion");
		btnLeft.addClickListener(executeArrowButton);
		btnLeft.setVisible(true);
		backgroundmain.addDisplayElement(btnLeft);
		
		btnRight = new UIModButtonArrow(new Vector2f(12,23), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.RIGHT, new Vector2f(-6, -54), "next miniion");
		btnRight.setPosition(new Vector2f(21,87));
		btnRight.setId("nextminion");
		btnRight.addClickListener(executeArrowButton);
		btnRight.setVisible(true);
		backgroundmain.addDisplayElement(btnRight);						
		
		GridLayout layout = new GridLayout(1);
        layout.setCellPadding(new Vector4f(0f, 0f, 0f, 0f));
		behaviourlist = new UIComposite();
		behaviourlist.setSize(new Vector2f(100,120));
		behaviourlist.setPosition(new Vector2f(200,132));
		behaviourlist.setBackgroundImage("miniion:modularback");
		behaviourlist.setLayout(layout);
		behaviourlist.setVisible(false);
		backgroundmain.addDisplayElement(behaviourlist);
		
		btnBehaviour  = new UIModButtonArrow(new Vector2f(46,12), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.DOWN, new Vector2f(-222, -83) ,"set behaviour");
		btnBehaviour.setPosition(new Vector2f(237,116));
		btnBehaviour.setVisible(true);
		btnBehaviour.setId("showbehaviour");
		btnBehaviour.addClickListener(executeArrowButton);		
		backgroundmain.addDisplayElement(btnBehaviour);
		
		btnStay = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnStay.setLabel("STAY");
		btnStay.setId("stay");
		btnStay.addClickListener(behaviourToggleListener);
		btnStay.setVisible(true);
		behaviourlist.addDisplayElement(btnStay);
		
		btnFollow = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnFollow.setLabel("FOLLOW");
		btnFollow.setId("foll");
		btnFollow.addClickListener(behaviourToggleListener);
		btnFollow.setVisible(true);
		behaviourlist.addDisplayElement(btnFollow);
		
		btnAttack = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnAttack.setLabel("ATTACK");
		btnAttack.setId("atta");
		btnAttack.addClickListener(behaviourToggleListener);
		btnAttack.setVisible(true);
		behaviourlist.addDisplayElement(btnAttack);
		
		btnGather = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnGather.setLabel("GATHER");
		btnGather.setId("gath");
		btnGather.addClickListener(behaviourToggleListener);
		btnGather.setVisible(true);
		behaviourlist.addDisplayElement(btnGather);
		
		btnWork = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnWork.setLabel("WORK");
		btnWork.setId("work");
		btnWork.addClickListener(behaviourToggleListener);
		btnWork.setVisible(true);
		behaviourlist.addDisplayElement(btnWork);
		
		btnTerra = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.TOGGLE);
		btnTerra.setLabel("Terrafrom");
		btnTerra.setId("terr");
		btnTerra.addClickListener(behaviourToggleListener);
		btnTerra.setVisible(true);
		behaviourlist.addDisplayElement(btnTerra);
		
		btnActions  = new UIModButtonArrow(new Vector2f(46,12), org.terasology.miniion.gui.UIModButtonArrow.ButtonType.DOWN, new Vector2f(-122, -83) ,"select action");
		btnActions.setPosition(new Vector2f(137,116));
		btnActions.setVisible(true);
		btnActions.setId("showactions");
		btnActions.addClickListener(executeArrowButton);		
		backgroundmain.addDisplayElement(btnActions);
		
		actionlist = new UIComposite();
		actionlist.setSize(new Vector2f(100,120));
		actionlist.setPosition(new Vector2f(100,132));
		actionlist.setBackgroundImage("miniion:modularback");
		actionlist.setLayout(layout);
		actionlist.setVisible(false);
		backgroundmain.addDisplayElement(actionlist);
		
		btnInventory = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnInventory.setLabel("inventory");
		btnInventory.setId("inve");
		btnInventory.addClickListener(actionListener);
		btnInventory.setVisible(true);
		actionlist.addDisplayElement(btnInventory);
		
		btnZone = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnZone.setLabel("zone");
		btnZone.setId("zone");
		btnZone.addClickListener(actionListener);
		btnZone.setVisible(true);
		actionlist.addDisplayElement(btnZone);
		
		btnRecipe = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnRecipe.setLabel("recipe");
		btnRecipe.setId("reci");
		btnRecipe.addClickListener(actionListener);
		btnRecipe.setVisible(true);
		actionlist.addDisplayElement(btnRecipe);
		
		btnMinionlist = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Minions, new Vector2f(8, 32), "Miniions management");
		btnMinionlist.setPosition(new Vector2f(7,1));
		btnMinionlist.setId("minman");
		btnMinionlist.addClickListener(actionListener);
		btnMinionlist.setVisible(true);
		backgroundmain.addDisplayElement(btnMinionlist);
		
		btnZoneMan = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Zones, new Vector2f(-33, 32), "Zone management");
		btnZoneMan.setPosition(new Vector2f(48,1));
		btnZoneMan.setId("zonmman");
		btnZoneMan.addClickListener(actionListener);
		btnZoneMan.setVisible(true);
		backgroundmain.addDisplayElement(btnZoneMan);
		
		btnMessage = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Messages, new Vector2f(-74, 32), "Message management");
		btnMessage.setPosition(new Vector2f(89,1));
		btnMessage.setId("mesman");
		btnMessage.addClickListener(actionListener);
		btnMessage.setVisible(true);
		backgroundmain.addDisplayElement(btnMessage);
		
		btnCraftMan = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Crafting, new Vector2f(-115, 32), "Crafting management");
		btnCraftMan.setPosition(new Vector2f(130,1));
		btnCraftMan.setId("craman");
		btnCraftMan.addClickListener(actionListener);
		btnCraftMan.setVisible(true);
		btnCraftMan.setEnabled(false);
		backgroundmain.addDisplayElement(btnCraftMan);
		
		btnBuildMan = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Building, new Vector2f(-156, 32), "Building management");
		btnBuildMan.setPosition(new Vector2f(171,1));
		btnBuildMan.setId("buiman");
		btnBuildMan.addClickListener(actionListener);
		btnBuildMan.setVisible(true);
		btnBuildMan.setEnabled(false);
		backgroundmain.addDisplayElement(btnBuildMan);
		
		btnEmpty = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.MiniMap, new Vector2f(-197, 32), "empty");
		btnEmpty.setPosition(new Vector2f(212,1));
		btnEmpty.setId("mapman");
		btnEmpty.addClickListener(actionListener);
		btnEmpty.setVisible(true);
		btnEmpty.setEnabled(false);
		backgroundmain.addDisplayElement(btnEmpty);
		
		btnSettings = new UIModButtonSpecial(new Vector2f(40,30), org.terasology.miniion.gui.UIModButtonSpecial.ButtonType.Settings, new Vector2f(-237, 32), "Miniions settings");
		btnSettings.setPosition(new Vector2f(253,1));
		btnSettings.setId("setman");
		btnSettings.addClickListener(actionListener);
		btnSettings.setVisible(true);
		backgroundmain.addDisplayElement(btnSettings);
		
		btnStats = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnStats.setLabel("statistics");
		btnStats.setId("showstats");
		btnStats.addClickListener(actionListener);
		btnStats.setVisible(true);
		actionlist.addDisplayElement(btnStats);
		
		btnClear = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnClear.setLabel("clear orders");
		btnClear.setId("clea");
		btnClear.addClickListener(actionListener);
		btnClear.setVisible(true);
		actionlist.addDisplayElement(btnClear);
		
		btnBye = new UIModButtonMenu(new Vector2f(100,20), org.terasology.miniion.gui.UIModButtonMenu.ButtonType.NORMAL);
		btnBye.setLabel("bye bye");
		btnBye.setId("byeb");
		btnBye.addClickListener(actionListener);
		btnBye.setVisible(true);
		actionlist.addDisplayElement(btnBye);				
		
		uiMainlist = new UIList();
		uiMainlist.setSize(new Vector2f(100, 300));
		uiMainlist.setPosition(new Vector2f(0, 132));
		uiMainlist.setBackgroundImage("miniion:modularback");
		uiMainlist.setVisible(false);
		this.addDisplayElement(uiMainlist);
		
		uiDetailList = new UIList();
		uiDetailList.setSize(new Vector2f(100, 300));
		uiDetailList.setPosition(new Vector2f(0, 132));
		uiDetailList.setBackgroundImage("miniion:modularback");
		uiDetailList.setVisible(false);
		this.addDisplayElement(uiDetailList);							
		
		uistats = new UIScreenStats();
		uistats.setSize(new Vector2f(300,600));
		//edit this to minus the width of the window if you wonna change the size
		uistats.setPosition(new Vector2f(-300,extrainfoheight));
		//we'll make a new background for the stats, but for now this will do
		uistats.setBackgroundImage("miniion:modularback");
		uistats.setVisible(false);
		backgroundmain.addDisplayElement(uistats);
		
		uisettings = new UIScreenSettings();
		uisettings.setSize(new Vector2f(300,600));
		uisettings.setPosition(new Vector2f(-300,extrainfoheight));
		uisettings.setBackgroundImage("miniion:modularback");
		uisettings.setVisible(false);
		backgroundmain.addDisplayElement(uisettings);
		
		
		
	}
	
	@Override
	public void open() {
		super.open();
		setModal(true);
		refreshScreen();
	}
	
	/**
	 * execute clicks on arrowbuttons
	 */
	private ClickListener executeArrowButton = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIModButtonArrow arrow = (UIModButtonArrow)element;
			if(arrow.getId() == "showbehaviour"){
				if(MinionSystem.getActiveMinion() != null){
					behaviourlist.setVisible(!behaviourlist.isVisible());
				}
			}else
			if(arrow.getId() == "showactions"){
				if(MinionSystem.getActiveMinion() != null){
					actionlist.setVisible(!actionlist.isVisible());
				}
			}else
			if(arrow.getId() == "previousminion"){
				MinionSystem.getPreviousMinion(false);
				refreshScreen();
			}else
			if(arrow.getId() == "nextminion"){
				MinionSystem.getNextMinion(false);
				refreshScreen();
			}
		}
	};
	
	private void closeAllFoldouts(){
		behaviourlist.setVisible(false);
		actionlist.setVisible(false);
		uistats.setVisible(false);
		uiMainlist.setVisible(false);
		uiDetailList.setVisible(false);
	}
			
	/**
	 * sets the clicked toggle button and matching behaviour
	 */
	private ClickListener behaviourToggleListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIModButtonMenu clickedbutton = (UIModButtonMenu) element;
			toggleBehaviour(clickedbutton);									
			if(MinionSystem.getActiveMinion() != null){
				MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
				if (clickedbutton.getId() == "stay") {
					minioncomp.minionBehaviour = MinionBehaviour.Stay;
				}else
				if (clickedbutton.getId() == "foll") {
					minioncomp.minionBehaviour = MinionBehaviour.Follow;
				}else
				if (clickedbutton.getId() == "atta") {
					minioncomp.minionBehaviour = MinionBehaviour.Attack;
				}else
				if (clickedbutton.getId() == "gath") {
					minioncomp.minionBehaviour = MinionBehaviour.Gather;
				}else
				if (clickedbutton.getId() == "work") {
					minioncomp.minionBehaviour = MinionBehaviour.Work;
				}else
				if (clickedbutton.getId() == "terr") {
					minioncomp.minionBehaviour = MinionBehaviour.Terraform;
				}
				MinionSystem.getActiveMinion().saveComponent(minioncomp);
			}
			behaviourlist.setVisible(false);
		}
	};
	
	/**
	 * sets the clicked toggle button and matching behaviour
	 */
	private ClickListener actionListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			if(element.getClass().equals(UIModButtonMenu.class)){
				UIModButtonMenu clickedbutton = (UIModButtonMenu) element;
				if(MinionSystem.getActiveMinion() != null){
					MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
					if (clickedbutton.getId() == "inve") {
						MinionSystem.getActiveMinion().send(new ActivateEvent(MinionSystem.getActiveMinion(), CoreRegistry.get(LocalPlayer.class).getEntity()));
					}else
					if (clickedbutton.getId() == "zone") {
						if(uiDetailList.isVisible()){
							uiDetailList.setVisible(false);
						}
						if(uiMainlist.isVisible()){
							uiMainlist.setVisible(false);
						}
						else{
							uiMainlist.removeAll();
							for (ZoneType zonetype : ZoneType.values()) {
								UIListItem listitem = new UIListItem(zonetype.toString(), zonetype);
								listitem.addClickListener(zoneItemListener);
								uiMainlist.addItem(listitem);
							}
							uiMainlist.setVisible(true);
						}
					}else
					if (clickedbutton.getId() == "reci") {
						uiMainlist.removeAll();
						if(uiMainlist.isVisible()){
							uiMainlist.setVisible(false);
						}
						else{
							for (MinionRecipe recipe : MinionSystem.getRecipesList()) {
								UIListItem listitem = new UIListItem(recipe.Name, recipe);
								listitem.addClickListener(recipeItemListener);
								uiMainlist.addItem(listitem);
							}
							uiMainlist.setVisible(true);
						}
					}else
					if(clickedbutton.getId() == "showstats"){
						if(MinionSystem.getActiveMinion() != null){
							if(uisettings.isVisible()){
								uisettings.setVisible(false);
							}
							uistats.setVisible(!uistats.isVisible());
						}
					}else	
					if (clickedbutton.getId() == "clea") {
						SimpleMinionAIComponent aicomp = MinionSystem.getActiveMinion().getComponent(SimpleMinionAIComponent.class);
						aicomp.ClearCommands();
						MinionSystem.getActiveMinion().saveComponent(aicomp);
						minioncomp.clearCommands();
						MinionSystem.getActiveMinion().saveComponent(minioncomp);
						refreshScreen();
					}else
					if (clickedbutton.getId() == "byeb") {
						//WARNING!!!! execute getprevious before setting the component to dying, 
						//else getprevious will have trouble determining what minion needs to become active!
						EntityRef dyingminion = MinionSystem.getActiveMinion();
						MinionSystem.getPreviousMinion(true);
						minioncomp.minionBehaviour = MinionBehaviour.Die;
						minioncomp.dying = true;
						dyingminion.saveComponent(minioncomp);
						closeAllFoldouts();
						refreshScreen();
					}					
				}
			}
			else if(element.getClass().equals(UIModButtonSpecial.class)){
				UIModButtonSpecial clickedbutton = (UIModButtonSpecial) element;
				if (clickedbutton.getId() == "setman") {
					if(uistats.isVisible()){
						uistats.setVisible(false);
					}
					uisettings.setVisible(!uisettings.isVisible());
				}else
				if (clickedbutton.getId() == "minman") {
					uiMainlist.removeAll();
					if(uiMainlist.isVisible()){
						uiMainlist.setVisible(false);
					}
					else{
						EntityManager entman = CoreRegistry.get(EntityManager.class);
						for (Entry<EntityRef, MinionComponent> minion : entman.iterateComponents(MinionComponent.class)) {
							UIListItem listitem = new UIListItem(minion.getValue().name, minion.getKey());
							listitem.addClickListener(minionlistItemListener);
							uiMainlist.addItem(listitem);
						}
						uiMainlist.setVisible(true);
					}
				}			
			}
		}
	};
	
	private ClickListener zoneItemListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			UIListItem listitem = (UIListItem) element;
			if(listitem.getValue().getClass().equals(ZoneType.class)){
				switch(((ZoneType)listitem.getValue())){
				case Gather: {
					fillDetailList(MinionSystem.getGatherZoneList());
					break;
				}
				case Terraform: {
					fillDetailList(MinionSystem.getTerraformZoneList());
					break;
				}
				case Work : {
					fillDetailList(MinionSystem.getWorkZoneList());
					break;
				}
				case Storage : {
					fillDetailList(MinionSystem.getStorageZoneList());
					break;
				}
				case OreonFarm : {
					fillDetailList(MinionSystem.getOreonFarmZoneList());
					break;
				}
				case Residential : {					
					fillDetailList(MinionSystem.getResidentialZoneList());
					break;
				}
				default : {					
					break;
				}
				}
			}else if(listitem.getValue().getClass().equals(Zone.class)){
				Zone selectedzone = (Zone) ((UIListItem)element).getValue();
				MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
				minioncomp.assignedzone = selectedzone;
				MinionSystem.getActiveMinion().saveComponent(minioncomp);
				uiDetailList.setVisible(false);
				refreshScreen();
			}
		}
	};
	
	private void fillDetailList(List<Zone> zonelist){
		uiDetailList.removeAll();
		for (Zone zone : zonelist) {
			UIListItem newlistitem = new UIListItem(zone.Name, zone);
			newlistitem.addClickListener(zoneItemListener);
			uiDetailList.addItem(newlistitem);
		}
		uiMainlist.setVisible(false);
		uiDetailList.setVisible(true);
	}
	
	private ClickListener recipeItemListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			MinionRecipe selectedrecipe = (MinionRecipe) ((UIListItem)element).getValue();
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			for (MinionRecipe recipe : MinionSystem.getRecipesList()) {
				if (recipe.Name.matches(selectedrecipe.Name)) {
					minioncomp.assignedrecipe = recipe;
				}
			}
			MinionSystem.getActiveMinion().saveComponent(minioncomp);
			uiMainlist.setVisible(false);
			refreshScreen();
		}
	};
	
	private ClickListener minionlistItemListener = new ClickListener() {
		
		@Override
		public void click(UIDisplayElement element, int button) {
			EntityRef selectedminion = (EntityRef) ((UIListItem)element).getValue();
			MinionSystem.setActiveMinion(selectedminion);
			refreshScreen();
		}
	};
	
	/**
	 * make sure only 1 toggle is active in this displaycontainer
	 * @param button
	 * 				the selected button
	 */
	private void toggleBehaviour(UIModButtonMenu button){
		for(UIDisplayElement modbutton : behaviourlist.getDisplayElements()){
			if(modbutton.equals(button)){
				((UIModButtonMenu)modbutton).setToggleState(true);
			}else{
				((UIModButtonMenu)modbutton).setToggleState(false);
			}
		}
	}
	
	private void refreshScreen(){
		if(MinionSystem.getActiveMinion() == null){
			if(MinionSystem.getSettings() != null){
				MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
				backgroundextra.setVisible(settingcomp.showExtraInfo);
			}
			// remove and add border for resize
			// would be nice if I could lock the size to default size			
			lblname.removeBorderSolid();		
			lblname.setText("No miniion is obeying you!");
			lblname.setBorderSolid(new Vector4f(2f, 2f, 2f, 2f), Color.magenta);
			lblflavor.setText("Get your Oreominions now!!! 75% off if you bought any other DLC");
			lblzone.setText("");
			lblrecipe.setText("");            
		}else {
			MinionComponent minioncomp = MinionSystem.getActiveMinion().getComponent(MinionComponent.class);
			// remove and add border for resize
			// would be nice if I could lock the size to default size
			lblname.removeBorderSolid();
			if(minioncomp == null){
				lblname.setText("missing component");
				lblflavor.setText("something went wrong, contact your system administrator! Quickly!");
			}else{
				lblname.setText(minioncomp.name);
				lblflavor.setText(minioncomp.flavortext);				
				if(minioncomp.assignedzone == null){
					lblzone.setText("no zone assigned");
				}else
				{
					lblzone.setText("workzone : " + minioncomp.assignedzone.Name);
					if(minioncomp.hasBuildPlan()) lblzone.appendText(" and has plan");
				}
				if(minioncomp.assignedrecipe == null){
					lblrecipe.setText("");
				}else
				{
					String tmpstr = " requires : ";
					for(String resource : minioncomp.assignedrecipe.craftRes){
						tmpstr = tmpstr.concat(resource + ", ");
					}					
					lblrecipe.setText("recipe : " + minioncomp.assignedrecipe.Name + tmpstr.substring(0, tmpstr.lastIndexOf(",")));
				}
				if (minioncomp.minionBehaviour == MinionBehaviour.Follow) {
					toggleBehaviour(btnFollow);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Stay) {
					toggleBehaviour(btnStay);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Attack) {
					toggleBehaviour(btnAttack);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Gather) {
					toggleBehaviour(btnGather);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Work) {
					toggleBehaviour(btnWork);
				} else if (minioncomp.minionBehaviour == MinionBehaviour.Terraform) {
					toggleBehaviour(btnTerra);		
				} else {
					toggleBehaviour(null);
				}
			}			
			lblname.setBorderSolid(new Vector4f(2f, 2f, 2f, 2f), Color.magenta);						
		}
		//refresh the stats screen whenever the main window refreshes.
		uistats.refreshScreen();
	}	
}
	
