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
package org.terasology.miniion.componentsystem.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.terasology.asset.Assets;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.LocalPlayerComponent;

import org.terasology.rendering.logic.*;
import org.terasology.world.block.*;
import org.terasology.entityFactory.*;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.events.*;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.input.binds.RadialButton;
import org.terasology.input.binds.UseItemButton;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.*;
import org.terasology.miniion.gui.*;
import org.terasology.miniion.utilities.*;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 10/05/12 Time: 17:54
 * Minionsystem gives you some control over the minions. this is the home of the
 * minionbar.
 */
@RegisterComponentSystem
public class MinionSystem implements EventHandlerSystem {

	@In
	private LocalPlayer localPlayer;
	@In
	private EntityManager entityManager;
	@In 
	private Timer timer;
	@In
	private GUIManager guiManager;
	
	//private static final int PRIORITY_LOCAL_PLAYER_OVERRIDE = 160;
	private static EntityRef activeminion;
	// TODO : a better way to save / load zones, but it does the trick
	private static EntityRef zonelist, settingslist;
	private static ZoneWithRender newzone;
		
	private static List<MinionRecipe> recipeslist = new ArrayList<MinionRecipe>();
	
	private BlockItemFactory blockItemFactory;
	
	private long lastInteraction;


	@Override
	public void initialise() {
		ModIcons.loadIcons();
		createSettings();
		lastInteraction = timer.getTimeInMs();
		blockItemFactory = new BlockItemFactory(entityManager);
		// experimental popup menu for the minion command tool
		guiManager.registerWindow("activeminiion", UIActiveMinion.class);
		// ui to create summonable cards
		guiManager.registerWindow("cardbook", UICardBook.class); 
		// ui to manage summoned minions, selecting one sets it active!
		//guiManager.registerWindow("oreobook", UIScreenBookOreo.class);
		// ui to manage zones
		guiManager.registerWindow("zonebook", UIZoneBook.class);
		guiManager.registerWindow("radialmain", UIRadial.class); 
		createZoneList();
		initRecipes();
	}
	
	//need to check inventory if already has one or not before sending this
    /*@ReceiveEvent(components = {LocalPlayerComponent.class})
    public void onSpawn(AddComponentEvent event, EntityRef entity) {
        entity.send(new ReceiveItemEvent(CoreRegistry.get(EntityManager.class).create("miniion:minioncommand")));
    }*/

	/**
	 * Ugly way to retrieve a name from a prefab
	 * 
	 * @return 
	 * 			a ":" seperated string, with name and flavor text.
	 */
	public static String getName() {
		PrefabManager prefMan = CoreRegistry.get(PrefabManager.class);
		Prefab prefab = prefMan.getPrefab("miniion:nameslist");
		EntityRef namelist = CoreRegistry.get(EntityManager.class).create(prefab);
		namelist.hasComponent(namesComponent.class);
		namesComponent namecomp = namelist.getComponent(namesComponent.class);
		Random rand = new Random();
		return namecomp.namelist.get(rand.nextInt(namecomp.namelist.size()));
	}

	/**
	 * destroys a minion at the end of their dying animation this implies that
	 * setting their animation to die will destroy them.
	 */
	@ReceiveEvent(components = { SkeletalMeshComponent.class,
			AnimationComponent.class })
	public void onAnimationEnd(AnimEndEvent event, EntityRef entity) {
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		if (animcomp != null && event.getAnimation().equals(animcomp.dieAnim)) {
			entity.destroy();
		}
	}

	/**
	 * triggered when a block was destroyed and dropped in the world used to
	 * intercept gathering by minions and sending the block to their inventory
	 * the droppedblock in the world then gets destroyed, possible duplication
	 * exploit
	 */
	@ReceiveEvent(components = { MinionComponent.class })
	public void onBlockDropped(BlockDroppedEvent event, EntityRef entity) {
		if (entity.hasComponent(MinionComponent.class)) {
			EntityRef item;
			if (event.getoldBlock().getEntityMode() == BlockEntityMode.PERSISTENT) {
				item = blockItemFactory.newInstance(event.getoldBlock()
						.getBlockFamily(), entity);
			} else {
				item = blockItemFactory.newInstance(event.getoldBlock()
						.getBlockFamily());
			}
			entity.send(new ReceiveItemEvent(item));
			event.getDroppedBlock().destroy();
		}
	}

	/**
	 * The active minion, to be commanded by the minion command item etc uses a
	 * slightly different texture to indicate selection
	 * 
	 * @param minion
	 *            : the new active minion entity
	 */
	public static void setActiveMinion(EntityRef minion) {
		SkeletalMeshComponent skelcomp;
		if (activeminion != null) {
			skelcomp = activeminion.getComponent(SkeletalMeshComponent.class);
			if (skelcomp != null) {
				skelcomp.material = Assets.getMaterial("OreoMinions:OreonSkin");
			}
		}
		skelcomp = minion.getComponent(SkeletalMeshComponent.class);
		if (skelcomp != null) {
			skelcomp.material = Assets
					.getMaterial("OreoMinions:OreonSkinSelected");
		}
		activeminion = minion;
	}

	/**
	 * returns the currently active minion
	 * 
	 * @return : the currently active minion
	 */
	public static EntityRef getActiveMinion() {
		return activeminion;
	}
	
	public static void startNewSelection(Vector3i startpos){
		newzone = new ZoneWithRender();
		newzone.setStartPosition(startpos);
	}

	public static void endNewSelection(Vector3i endpos){
		if(newzone != null){
			newzone.setEndPosition(endpos);
		}
	}

	public static void resetNewSelection(){
		newzone = null;
	}

	public static ZoneWithRender getNewZone(){
		return newzone;
	}

	/**
	 * adds a new zone to the corresponding zone list
	 * @param zone
	 * 				the zone to be added
	 */
	public static void addZone(Zone zone) {		
		ZoneListComponent zonelistcomp = zonelist.getComponent(ZoneListComponent.class);
		switch(zone.zonetype){
			case Gather : {
				zonelistcomp.Gatherzones.add(zone);
				break;
			}
			case Work : {
				zonelistcomp.Workzones.add(zone);
				break;
			}
			case Terraform : {
				zonelistcomp.Terrazones.add(zone);
				break;
			}
			case Storage : {
				zonelistcomp.Storagezones.add(zone);
				break;
			}
			case OreonFarm : {
				zonelistcomp.OreonFarmzones.add(zone);
				break;
			}
			case Residential : {
				zonelistcomp.Residentialzones.add(zone);
				break;
			}
		}		
		zonelist.saveComponent(zonelistcomp);
	}

	/**
	 * returns a list with all gather zones
	 * @return
	 * 			a list with all gather zones
	 */
	public static List<Zone> getGatherZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).Gatherzones;
	}
	
	/**
	 * returns a list with all work zones
	 * @return
	 * 			a list with all work zones
	 */
	public static List<Zone> getWorkZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).Workzones;
	}
	
	/**
	 * returns a list with all terraform zones
	 * @return
	 * 			a list with all terraform zones
	 */
	public static List<Zone> getTerraformZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).Terrazones;
	}
	
	/**
	 * returns a list with all storage zones
	 * @return
	 * 			a list with all storage zones
	 */
	public static List<Zone> getStorageZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).Storagezones;
	}
	
	/**
	 * returns a list with all Oreon farm zones
	 * @return
	 * 			a list with all Oreon farm zones
	 */
	public static List<Zone> getOreonFarmZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).OreonFarmzones;
	}
	
	/**
	 * returns a list with all Residential zones
	 * @return
	 * 			a list with all Residential zones
	 */
	public static List<Zone> getResidentialZoneList() {
		if (zonelist == null) {
			return null;
		}
		return zonelist.getComponent(ZoneListComponent.class).Residentialzones;
	}
	
	public static List<MinionRecipe> getRecipesList(){
		if(recipeslist == null){
			return null;
		}
		return recipeslist;
	}

	/**
	 * creates a zonelist component
	 * used to save all zones (persist)
	 */
	private static void createZoneList() {
		zonelist = CoreRegistry.get(EntityManager.class).create();
		ZoneListComponent zonecomp = new ZoneListComponent();
		zonelist.addComponent(zonecomp);
		zonelist.setPersisted(true);
		zonelist.saveComponent(zonecomp);
	}
	
	/**
	 * creates a settings component
	 * used to save settings (persist)
	 */
	private static void createSettings() {
		settingslist = CoreRegistry.get(EntityManager.class).create();
		MiniionSettingsComponent settingcomp = new MiniionSettingsComponent();
		settingslist.addComponent(settingcomp);
		settingslist.setPersisted(true);
		settingslist.saveComponent(settingcomp);
	}
	
	public static EntityRef getSettings(){
		return settingslist;
	}	
	
	/**
	 * order the minions by id and return the next one, or the first if none were active!
	 */
	public static void getNextMinion(boolean deleteactive){
		EntityManager entman = CoreRegistry.get(EntityManager.class);
		List<Integer> sortedlist = new ArrayList<Integer>();		
		for(EntityRef minion : entman.iteratorEntities(MinionComponent.class)){
			if(!minion.getComponent(MinionComponent.class).dying){
				sortedlist.add(minion.getId());
			}
		}
		if(sortedlist.size() ==0){
			return;
		}else if(deleteactive && sortedlist.size() == 1){
			activeminion = null;
			return;
		}
		Collections.sort(sortedlist);
		int index = 0;
		if(activeminion != null){			
			index = sortedlist.indexOf(activeminion.getId());
		}		
		if(index + 1 == sortedlist.size()){
			index = 0;
		}else{
			index++;
		}
		index = sortedlist.get(index);
		for(EntityRef minion : entman.iteratorEntities(MinionComponent.class)){
			if(minion.getId() == index){
				setActiveMinion(minion);
			}
		}
	}
	
	/**
	 * order the minions by id and return the previous one, or the first if none were active!
	 */
	public static void getPreviousMinion(boolean deleteactive){
		EntityManager entman = CoreRegistry.get(EntityManager.class);
		List<Integer> sortedlist = new ArrayList<Integer>();		
		for(EntityRef minion : entman.iteratorEntities(MinionComponent.class)){
			if(!minion.getComponent(MinionComponent.class).dying){
				sortedlist.add(minion.getId());
			}
		}
		if(sortedlist.size() ==0){
			return;
		}else if(deleteactive && sortedlist.size() == 1){
			activeminion = null;
			return;
		}
		Collections.sort(sortedlist);
		int index = 0;
		if(activeminion != null){			
			index = sortedlist.indexOf(activeminion.getId());
		}		
		if(index == 0){
			index = sortedlist.size() -1;
		}else{
			index--;
		}
		index = sortedlist.get(index);
		for(EntityRef minion : entman.iteratorEntities(MinionComponent.class)){
			if(minion.getId() == index){
				setActiveMinion(minion);
			}
		}
	}
	
	private void initRecipes(){
		MinionRecipe recipe = new MinionRecipe();
		
		recipe.Name = "Chocolate";
		recipe.result = "CakeLie:ChocolateBlock";
		recipe.craftRes.add("dirt");
		recipe.craftsteps = 20;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Brownie";
		recipe.result = "CakeLie:ChocolateBrownieBlock";
		recipe.craftRes.add("stone");
		recipe.craftsteps = 35;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Brownie top";
		recipe.result = "CakeLie:ChocolateBrownieTop";
		recipe.craftRes.add("stone");
		recipe.craftRes.add("snow");
		recipe.craftsteps = 60;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Kiwi cake";
		recipe.result = "CakeLie:KiwiCakeBlock";
		recipe.craftRes.add("cactus");
		recipe.craftsteps = 30;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Kiwi top";
		recipe.result = "CakeLie:KiwiCakeTop";
		recipe.craftRes.add("cactus");
		recipe.craftRes.add("snow");
		recipe.craftsteps = 50;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Corny choco";
		recipe.result = "CakeLie:CornyChokoBlock";
		recipe.craftRes.add("grass");
		recipe.craftsteps = 30;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Corny top";
		recipe.result = "CakeLie:CornyChokoTop";
		recipe.craftRes.add("grass");
		recipe.craftRes.add("snow");
		recipe.craftsteps = 50;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Orange cake";
		recipe.result = "CakeLie:OrangeCakeBlock";
		recipe.craftRes.add("sand");
		recipe.craftsteps = 30;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "Orange Top";
		recipe.result = "CakeLie:OrangeCakeTop";
		recipe.craftRes.add("sand");
		recipe.craftRes.add("snow");
		recipe.craftsteps = 50;
		recipeslist.add(recipe);
		
		recipe = new MinionRecipe();
		recipe.Name = "chest";
		recipe.result = "core:chest";
		recipe.craftRes.add("plank");
		recipe.quantity = 8;
		recipe.craftsteps = 100;
		recipeslist.add(recipe);
	} 	

	@Override
	public void shutdown() {
		if(activeminion != null){
			SkeletalMeshComponent skelcomp = activeminion.getComponent(SkeletalMeshComponent.class);
			if (skelcomp != null) {
				skelcomp.material = Assets.getMaterial("OreoMinions:OreonSkin");
			}
		}
		MiniionSettingsComponent settingcomp = settingslist.getComponent(MiniionSettingsComponent.class);
		settingslist.saveComponent(settingcomp);
	}
	
	@ReceiveEvent(components = {LocalPlayerComponent.class, InventoryComponent.class}, priority = EventPriority.PRIORITY_NORMAL)
    public void onUseItemRequest(RadialButton event, EntityRef entity) {
			
        if (!event.isDown() || timer.getTimeInMs() - lastInteraction < 200) {
            return;
        }
        CoreRegistry.get(GUIManager.class).openWindow("radialmain");
        /*
        LocalPlayerComponent localPlayerComp = entity.getComponent(LocalPlayerComponent.class);
        InventoryComponent inventory = entity.getComponent(InventoryComponent.class);
        if (localPlayerComp.isDead) return;

        EntityRef selectedItemEntity = inventory.itemSlots.get(localPlayerComp.selectedTool);

        ItemComponent item = selectedItemEntity.getComponent(ItemComponent.class);
        if (item != null && item.usage != ItemComponent.UsageType.NONE) {
            useItem(event.getTarget(), entity, selectedItemEntity, event.getHitPosition(), event.getHitNormal());
        }

        lastInteraction = timer.getTimeInMs();
        localPlayerComp.handAnimation = 0.5f;
        entity.saveComponent(localPlayerComp);
        */
        event.consume();
    }

	/**
	 * overrides the default attack event if the minion command item is the
	 * current helditem only adds gather targets for now, minion command needs
	 * popuup to set behaviour
	 */
	/*@ReceiveEvent(components = { LocalPlayerComponent.class, MinionControllerComponent.class }, priority = PRIORITY_LOCAL_PLAYER_OVERRIDE)
	public void onAttack(AttackButton event, EntityRef entity) {
		LocalPlayerComponent locplaycomp = entity
				.getComponent(LocalPlayerComponent.class);
		UIItemContainer toolbar = (UIItemContainer) CoreRegistry
				.get(GUIManager.class).getWindowById("hud")
				.getElementById("toolbar");
		int invSlotIndex = localPlayer.getEntity().getComponent(
				LocalPlayerComponent.class).selectedTool
				+ toolbar.getSlotStart();
		EntityRef heldItem = localPlayer.getEntity().getComponent(
				InventoryComponent.class).itemSlots.get(invSlotIndex);
		ItemComponent heldItemComp = heldItem.getComponent(ItemComponent.class);

		if (heldItemComp != null && activeminion != null
				&& heldItemComp.name.matches("Minion Command")) {
			SimpleMinionAIComponent aicomp = activeminion
					.getComponent(SimpleMinionAIComponent.class);
			LocationComponent loccomp = event.getTarget().getComponent(
					LocationComponent.class);
			if(loccomp != null)
			{
				aicomp.gatherTargets.add(loccomp.getWorldPosition());
			}
			activeminion.saveComponent(aicomp);
			locplaycomp.handAnimation = 0.5f;
			entity.saveComponent(locplaycomp);
			event.consume();
		}
	}*/
}
