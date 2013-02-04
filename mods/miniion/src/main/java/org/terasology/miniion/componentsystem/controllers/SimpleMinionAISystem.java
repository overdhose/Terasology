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

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Vector3f;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.componentSystem.UpdateSubscriberSystem;
import org.terasology.componentSystem.worldSimulation.GrowthSimulator;
import org.terasology.components.InventoryComponent;
import org.terasology.components.ItemComponent;
import org.terasology.components.world.LocationComponent;
import org.terasology.entityFactory.BlockItemFactory;
import org.terasology.entitySystem.*;
import org.terasology.entitySystem.event.AddComponentEvent;
import org.terasology.events.DamageEvent;
import org.terasology.events.HorizontalCollisionEvent;
import org.terasology.events.inventory.ReceiveItemEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.game.Timer;
import org.terasology.logic.LocalPlayer;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.*;
import org.terasology.miniion.grammar.YummyGenerator;
import org.terasology.miniion.pathfinder.AStarPathing;
import org.terasology.miniion.minionenum.MinionMessagePriority;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.miniion.utilities.*;
import org.terasology.model.structures.BlockPosition;
import org.terasology.model.structures.BlockSelection;
import org.terasology.physics.character.CharacterMovementComponent;
import org.terasology.rendering.assets.animation.MeshAnimation;
import org.terasology.rendering.logic.SkeletalMeshComponent;
import org.terasology.world.*;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockItemComponent;
import org.terasology.world.block.management.BlockManager;

import com.google.common.collect.Queues;

/**
 * Created with IntelliJ IDEA. User: Overdhose Date: 7/05/12 Time: 18:25 first
 * evolution of the minion AI, could probably use a lot of improvements
 */
@RegisterComponentSystem
public class SimpleMinionAISystem implements EventHandlerSystem,
		UpdateSubscriberSystem {

	private static final Logger logger = LoggerFactory.getLogger(SimpleMinionAISystem.class);
	private static final YummyGenerator buildgenerator = new YummyGenerator();
	
	private EntityManager entityManager;
	private WorldProvider worldProvider;
	private BlockEntityRegistry blockEntityRegistry;
	private AStarPathing aStarPathing;
	private Timer timer;
	private ExecutorService executor;
	private BlockingQueue<EntityRef> blockQueue;
	private AtomicBoolean running = new AtomicBoolean();
	
	@Override
	public void initialise() {
		entityManager = CoreRegistry.get(EntityManager.class);
		worldProvider = CoreRegistry.get(WorldProvider.class);
		blockEntityRegistry = CoreRegistry.get(BlockEntityRegistry.class);
		timer = CoreRegistry.get(Timer.class);
		aStarPathing = new AStarPathing(worldProvider);
		
		running.set(true);
		blockQueue = Queues.newLinkedBlockingQueue();
		executor = Executors.newFixedThreadPool(1);
		executor.execute(new Runnable() {
	            @Override
	            public void run() {
	                Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	                EntityRef minion;
	                while (running.get()) {
						try {
							minion = blockQueue.take();
							if(minion != null){
								MinionComponent minioncomp = minion.getComponent(MinionComponent.class);
				                Zone zone = minioncomp.assignedzone;
				                Random rand = new Random();
			                    minioncomp.setBuildPlan(buildgenerator.getYummyStructure(zone.zonewidth, 6, zone.zonedepth, rand.nextInt(10)));
							}
						} catch (InterruptedException e) {
							logger.debug("Building thread interrupted");
						}
	                }
	            }
		 });
	}
	
	 @ReceiveEvent(components = {SimpleMinionAIComponent.class})
	    public void onSpawn(AddComponentEvent event, EntityRef entity) {
		 	initMinionAI();
	    }
	
	private void initMinionAI(){
		//add 3000 to init to create  bit of a delay before first check
		long initTime = timer.getTimeInMs() +3000;
		for(EntityRef minion : entityManager.iteratorEntities(SimpleMinionAIComponent.class)){
			SimpleMinionAIComponent ai = minion.getComponent(SimpleMinionAIComponent.class);
			ai.lastAttacktime = initTime;
			ai.lastDistancecheck = initTime;
			ai.lastHungerCheck = initTime;
			minion.saveComponent(ai);
		}
	}

	@Override
	public void shutdown() {
		running.set(false);
        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            logger.error("Interrupted awaiting shutdown", e);
        }
        if (blockQueue.isEmpty()) {
            executor.shutdownNow();
        }
	}

	@Override
	public void update(float delta) {
		for (EntityRef entity : entityManager.iteratorEntities(
				SimpleMinionAIComponent.class,
				CharacterMovementComponent.class, LocationComponent.class,
				MinionComponent.class, SkeletalMeshComponent.class,
				AnimationComponent.class)) {

			CharacterMovementComponent moveComp = entity
					.getComponent(CharacterMovementComponent.class);
			MinionComponent minioncomp = entity
					.getComponent(MinionComponent.class);
			AnimationComponent animcomp = entity
					.getComponent(AnimationComponent.class);
			SimpleMinionAIComponent ai = entity
					.getComponent(SimpleMinionAIComponent.class);

			//hunger system, increase the delay by increasing > 10000
			if (timer.getTimeInMs() - ai.lastHungerCheck > 10000) {
				ai.lastHungerCheck = timer.getTimeInMs();
				if(minioncomp.Hunger > 0){
					minioncomp.Hunger-- ;
					//need to save components for data to persist when game restarts
					entity.saveComponent(minioncomp);
				}else{
					minioncomp.Hunger = 100;
				}
			}
			
			switch (minioncomp.minionBehaviour) {
			case Follow: {
				executeFollowAI(entity);
				break;
			}
			case Gather: {
				executeGatherAI(entity);
				break;
			}
			case Work: {
				executeWorkAI(entity);
				break;
			}
			case Terraform: {
				executeTerraformAI(entity, "");
				break;
			}
			case Move: {
				executeMoveAI(entity);
				break;
			}
			case Patrol: {
				executePatrolAI(entity);
				break;
			}
			case Attack: {
				changeAnimation(entity, animcomp.attackAnim, true);
				break;
			}
			case Die: {
				changeAnimation(entity, animcomp.dieAnim, false);
				break;
			}
			case Stay: {
				changeAnimation(entity, animcomp.idleAnim, false);
				break;
			}
			case Test: {
				executeTestAI(entity);
				break;
			}
			default: {
				changeAnimation(entity, animcomp.idleAnim, false);
				break;
			}
			}
		}
	}

	private void changeAnimation(EntityRef entity, MeshAnimation animation,
			boolean loop) {
		SkeletalMeshComponent skeletalcomp = entity
				.getComponent(SkeletalMeshComponent.class);
		if (skeletalcomp.animation != animation) {
			skeletalcomp.animation = animation;
			skeletalcomp.loop = loop;
			entity.saveComponent(skeletalcomp);
		}
	}

	private void randomAnimation(EntityRef entity,
			SkeletalMeshComponent skeletalcomp, AnimationComponent animcomp) {
	}

	private void executeFollowAI(EntityRef entity) {
		LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
		if (localPlayer == null) {
			return;
		}
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		Vector3f dist = new Vector3f(worldPos);
		dist.sub(localPlayer.getPosition());
		double distanceToPlayer = dist.lengthSquared();

		if (distanceToPlayer > 8) {
			// Head to player
			Vector3f target = localPlayer.getPosition();
			ai.movementTarget.set(target);
			ai.followingPlayer = true;
			entity.saveComponent(ai);
		}

		setMovement(ai.movementTarget, entity);
	}

	private void executeGatherAI(EntityRef entity) {
		MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		if (ai.gatherTargets.size() == 0 && minioncomp.assignedzone != null && minioncomp.assignedzone.zonetype == ZoneType.Gather) {
			getTargetsfromZone(minioncomp, ai);
		}

		if ((ai.gatherTargets == null) || (ai.gatherTargets.size() < 1)) {
			return;
		}
		Vector3f currentTarget = ai.gatherTargets.get(0);
		if (currentTarget == null) {
			ai.gatherTargets.remove(currentTarget);
			changeAnimation(entity, animcomp.idleAnim, true);
			entity.saveComponent(ai);
			return;
		}
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.lengthSquared();

		if (distanceToTarget < 4) {
			// switch animation
			changeAnimation(entity, animcomp.workAnim, false);
			// gather the block
			if (timer.getTimeInMs() - ai.lastAttacktime > 500) {
				ai.lastAttacktime = timer.getTimeInMs();
				boolean attacked = attack(entity, currentTarget);
				if (!attacked) {
					changeAnimation(entity, animcomp.idleAnim, true);
					ai.gatherTargets.remove(currentTarget);
				}
			}
		}

		entity.saveComponent(ai);
		setMovement(currentTarget, entity);
	}

	private void getTargetsfromZone(MinionComponent minioncomp,
			SimpleMinionAIComponent ai) {
		Zone zone = minioncomp.assignedzone;
		// first loop at highest blocks (y)
		for (int y = zone.getMaxBounds().y; y >= zone.getMinBounds().y; y--) {
			for (int x = zone.getMinBounds().x; x <= zone.getMaxBounds().x; x++) {
				for (int z = zone.getMinBounds().z; z <= zone.getMaxBounds().z; z++) {
					Block tmpblock = worldProvider.getBlock(x, y, z);
					if (!tmpblock.isInvisible()) {
						ai.gatherTargets.add(new Vector3f(x, y + 0.5f, z));
					}
				}
			}
		}
	}
	
	private void executeWorkAI(EntityRef entity) {
		MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());
		
		if(minioncomp.assignedzone == null){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}else if(minioncomp.assignedzone.getStartPosition() == null){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}else if(minioncomp.assignedzone.zonetype != ZoneType.Work){
			//redirect to farming steps
			if(minioncomp.assignedzone.zonetype == ZoneType.OreonFarm){
				if(minioncomp.assignedzone.isActionComplete()){
					//check if farm has unplanted spots
					if(minioncomp.assignedzone.checkFarm(false)){
						executeFarmmAI(entity);
					}else if(minioncomp.assignedzone.checkFarm(true)){
						//tend to the crop.
					}else{
						//nothing to do, go fish
						changeAnimation(entity, animcomp.idleAnim, true);
					}
				}else{
					executeTerraformAI(entity, "miniion:OreonFarmDry");
				}
			}
			else if(minioncomp.assignedzone.zonetype == ZoneType.Residential){
				//redirect to building.
				if(minioncomp.assignedzone != null && !minioncomp.hasBuildPlan()){
					changeAnimation(entity, animcomp.idleAnim, true);
                	if(!minioncomp.isQueued() && !minioncomp.assignedzone.isActionComplete()){
                		minioncomp.setQueued();
                		blockQueue.offer(entity);                		
                	}
				}else if(minioncomp.hasBuildPlan()){
					executeBuildAI(entity);
				}
			}
			else{
				changeAnimation(entity, animcomp.idleAnim, true);
				
			}
			
			return;
		}
		
		Vector3f currentTarget = minioncomp.assignedzone.getStartPosition().toVector3f();
		currentTarget.y += 0.5;
		
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.lengthSquared();

		if (distanceToTarget < 4) {
			
			// gather the block
			if (timer.getTimeInMs() - ai.lastAttacktime > 1000) {
				ai.lastAttacktime = timer.getTimeInMs();
				//increase craft progress
				boolean hasResources = false;
				if(minioncomp.assignedrecipe != null){
					InventoryComponent invcomp = entity.getComponent(InventoryComponent.class);
					for(String simpleuri : minioncomp.assignedrecipe.craftRes){
						hasResources = false;
						for(EntityRef slot : invcomp.itemSlots){
							if(slot != null){
								BlockItemComponent blockItem = slot.getComponent(BlockItemComponent.class);
								if(blockItem != null){
									if(blockItem.blockFamily.getURI().getFamily().matches(simpleuri)){
										hasResources = true;
										break;
									}
								}
							}
						}
						if(!hasResources){
							break;
						}
					}
					if(hasResources){
						// switch animation
						changeAnimation(entity, animcomp.workAnim, false);
						ai.craftprogress++;
						if(ai.craftprogress >= minioncomp.assignedrecipe.craftsteps){
							for(String simpleuri : minioncomp.assignedrecipe.craftRes){
								for(EntityRef slot : invcomp.itemSlots){
									if(slot != null){
										BlockItemComponent blockItem = slot.getComponent(BlockItemComponent.class);
										ItemComponent item = slot.getComponent(ItemComponent.class);
										if(blockItem != null){
											if(blockItem.blockFamily.getURI().getFamily().matches(simpleuri)){
												if(item.stackCount >= minioncomp.assignedrecipe.quantity)
												{
													item.stackCount -= minioncomp.assignedrecipe.quantity;
													if(item.stackCount == 0){
														slot.destroy();
													}
													break;
												}
											}
										}
									}
								}
							}
							Block recipeBlock = null;
							EntityRef result = EntityRef.NULL;
				            BlockItemFactory blockFactory = new BlockItemFactory(entityManager);
				            result = blockFactory.newInstance(BlockManager.getInstance().getBlockFamily(minioncomp.assignedrecipe.result));
							entity.send(new ReceiveItemEvent(result));
							ai.craftprogress = 0;
						}
					}
				}
			}
		}

		entity.saveComponent(ai);
		setMovement(currentTarget, entity);
	}
	
	/**
	 * Terraforms a zone into a set recipe, by default chocolate.
	 * @param entity 
	 * 				the minion that is terraforming
	 * @param fixedrecipeuri
	 * 				set to empty string by default for normal terraforming, 
	 * 				can override the default recipe for farming. 
	 */
	private void executeTerraformAI(EntityRef entity, String fixedrecipeuri) {
		MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());
		
		if(minioncomp.assignedzone == null){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}else if(minioncomp.assignedzone.getStartPosition() == null){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}else if(minioncomp.assignedzone.zonetype != ZoneType.Terraform && fixedrecipeuri.isEmpty()){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}else if(minioncomp.assignedzone.zonetype != ZoneType.OreonFarm){
			changeAnimation(entity, animcomp.idleAnim, true);
			return;
		}
		
		if (ai.movementTargets.size() == 0) {
			if(minioncomp.assignedzone.zonetype == ZoneType.Terraform){
				getFirsBlockfromZone(minioncomp, ai);
			}else
			if(minioncomp.assignedzone.zonetype == ZoneType.OreonFarm){
				getUnplantedBlockfromZone(minioncomp, ai);
			}
		}
		
		Vector3f currentTarget = ai.movementTargets.get(0);
		if (currentTarget == null) {
			ai.movementTargets.remove(currentTarget);
			changeAnimation(entity, animcomp.idleAnim, true);
			entity.saveComponent(ai);
			return;
		}		
		
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.lengthSquared();

		if (distanceToTarget < 4) {			
			// terraform
			changeAnimation(entity, animcomp.terraformAnim, true);
			if (timer.getTimeInMs() - ai.lastAttacktime > 200) {
				ai.lastAttacktime = timer.getTimeInMs();
				for(int y = (int)(currentTarget.y - 0.5); y >= minioncomp.assignedzone.getMinBounds().y; y--){
					Block tmpblock = worldProvider.getBlock((int)currentTarget.x, y, (int)currentTarget.z);
					if (!tmpblock.isInvisible()) {
						if(tmpblock.getBlockFamily().getURI().getPackage().equals("engine")){
							ai.craftprogress++;							
							if(ai.craftprogress > 20){
								Block newBlock;
								if(!fixedrecipeuri.isEmpty()){
									newBlock = BlockManager.getInstance().getBlock(fixedrecipeuri);
								}else
								if(minioncomp.assignedrecipe == null){
									newBlock = BlockManager.getInstance().getBlock("CakeLie:ChocolateBlock");
								}else
								{
									newBlock = BlockManager.getInstance().getBlock(minioncomp.assignedrecipe.result);
								}
								worldProvider.setBlock((int)currentTarget.x, y, (int)currentTarget.z, newBlock, tmpblock);
								ai.craftprogress = 0;
								if(y == minioncomp.assignedzone.getMinBounds().y){
									ai.movementTargets.remove(currentTarget);
								}
							}
							break;
						}else
						{
							if(y == minioncomp.assignedzone.getMinBounds().y){
								ai.movementTargets.remove(currentTarget);
							}
						}
					}else
					{
						if(y == minioncomp.assignedzone.getMinBounds().y){
							ai.movementTargets.remove(currentTarget);
						}
					}
					if(ai.movementTargets.size() == 0 && !fixedrecipeuri.isEmpty()){
						minioncomp.assignedzone.setActionComplete();
					}
				}
				
			}
		}

		entity.saveComponent(ai);
		setMovement(currentTarget, entity);
	}
	
	/**
	 * returns the top block for all positions
	 * @param minioncomp
	 * @param ai
	 */
	private void getFirsBlockfromZone(MinionComponent minioncomp,	SimpleMinionAIComponent ai) {
		Zone zone = minioncomp.assignedzone;		
		for (int x = zone.getMinBounds().x; x <= zone.getMaxBounds().x; x++) {
			for (int z = zone.getMinBounds().z; z <= zone.getMaxBounds().z; z++) {
				for (int y = zone.getMaxBounds().y; y >= zone.getMinBounds().y; y--) {
					Block tmpblock = worldProvider.getBlock(x, y, z);
					if (!tmpblock.isInvisible()) {
						ai.movementTargets.add(new Vector3f(x, (y + 0.5f), z));
						break;
					}
				}
			}
		}
	}
	
	/**
	 * returns the top block for all positions that have air above it
	 * @param minioncomp
	 * @param ai
	 */
	private void getUnplantedBlockfromZone(MinionComponent minioncomp,	SimpleMinionAIComponent ai) {
		Zone zone = minioncomp.assignedzone;		
		for (int x = zone.getMinBounds().x; x <= zone.getMaxBounds().x; x++) {
			for (int z = zone.getMinBounds().z; z <= zone.getMaxBounds().z; z++) {
				for (int y = zone.getMaxBounds().y; y >= zone.getMinBounds().y; y--) {
					Block tmpblock = worldProvider.getBlock(x, y, z);
					if (!tmpblock.isInvisible()) {
						tmpblock = worldProvider.getBlock(x, y+1, z);
						if (tmpblock.getURI().getFamily().matches("air")) {
							ai.movementTargets.add(new Vector3f(x, (y + 0.5f), z));
							break;
						}
					}
				}
			}
		}
	}
	
	/**
	 * plants crops
	 * @param entity 
	 * 				the minion that is terraforming
	 * @param fixedrecipeuri
	 * 				set to empty string by default for normal terraforming, 
	 * 				can override the default recipe for farming. 
	 */
	private void executeFarmmAI(EntityRef entity) {
		MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());
		
		if (ai.movementTargets.size() == 0) {
			getFirsBlockfromZone(minioncomp, ai);
			if (ai.movementTargets.size() == 0){
				return;
			}
		}
		
		Vector3f currentTarget = ai.movementTargets.get(0);
		if (currentTarget == null) {
			ai.movementTargets.remove(currentTarget);
			changeAnimation(entity, animcomp.idleAnim, true);
			entity.saveComponent(ai);
			return;
		}
		
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.lengthSquared();

		if (distanceToTarget < 4) {			
			// terraform
			changeAnimation(entity, animcomp.terraformAnim, true);
			if (timer.getTimeInMs() - ai.lastAttacktime > 200) {
				ai.lastAttacktime = timer.getTimeInMs();
				for(int y = (int)(currentTarget.y - 0.5); y >= minioncomp.assignedzone.getMinBounds().y; y--){
					Block tmpblock = worldProvider.getBlock((int)currentTarget.x, y + 1, (int)currentTarget.z);
					ai.craftprogress++;							
					if(ai.craftprogress > 20){
						Block newBlock;
						newBlock = BlockManager.getInstance().getBlock("miniion:OreonPlant0");
						worldProvider.setBlock((int)currentTarget.x, y+1, (int)currentTarget.z, newBlock, tmpblock);
						ai.craftprogress = 0;
						if(y == minioncomp.assignedzone.getMinBounds().y){
							ai.movementTargets.remove(currentTarget);
						}
					}					
				}				
			}
		}

		entity.saveComponent(ai);
		setMovement(currentTarget, entity);
	}
	
	/**
	 * create buildings based on grammar
	 * @param entity 
	 * 				the minion that is building
	 */
	private void executeBuildAI(EntityRef entity) {
		MinionComponent minioncomp = entity.getComponent(MinionComponent.class);
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());
		
		if(!minioncomp.assignedzone.isActionComplete()){
			if (timer.getTimeInMs() - ai.lastAttacktime > 200) {
				changeAnimation(entity, animcomp.workAnim, true);
				ai.lastAttacktime = timer.getTimeInMs();
				for(BlockPosition position : minioncomp.getBuildPlan().getBlocks().keySet()){	
					int x = minioncomp.assignedzone.getStartPosition().x +  position.x;
		            int y = minioncomp.assignedzone.getStartPosition().y +  position.y +1;
		            int z = minioncomp.assignedzone.getStartPosition().z +  position.z;
					worldProvider.setBlock(x, y, z, minioncomp.getBuildPlan().getBlock(position), worldProvider.getBlock(x, y, z));
					minioncomp.getBuildPlan().getBlocks().remove(position);
					if(minioncomp.getBuildPlan().getBlocks().isEmpty()){
						minioncomp.setBuildPlan(null);
						minioncomp.assignedzone.setActionComplete();
					}
					break;
				}
			}
		}
		
	}

	private void executeMoveAI(EntityRef entity) {
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		// get targets, break if none
		List<Vector3f> targets = ai.movementTargets;
		if ((targets == null) || (targets.size() < 1)) {
			return;
		}
		Vector3f currentTarget = targets.get(0);
		// trying to solve distance calculation with some simple trick of
		// reducing the height to 0.5, might not work for taller entities
		worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;

		// calc distance to current Target
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.length();

		// used 1.0 here as a check, should be lower to have the minion jump on
		// the last block, TODO need to calc middle of block
		if (distanceToTarget < 0.1d) {
			ai.movementTargets.remove(0);
			entity.saveComponent(ai);
			currentTarget = null;
			return;
		}

		setMovement(currentTarget, entity);
	}

	private void executePatrolAI(EntityRef entity) {
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		// get targets, break if none
		List<Vector3f> targets = ai.patrolTargets;
		if ((targets == null) || (targets.size() < 1)) {
			return;
		}
		int patrolCounter = ai.patrolCounter;
		Vector3f currentTarget = null;

		// get the patrol point
		if (patrolCounter < targets.size()) {
			currentTarget = targets.get(patrolCounter);
		}

		if (currentTarget == null) {
			return;
		}

		// calc distance to current Target
		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.length();

		if (distanceToTarget < 0.1d) {
			patrolCounter++;
			if (!(patrolCounter < targets.size()))
				patrolCounter = 0;
			ai.patrolCounter = patrolCounter;
			entity.saveComponent(ai);
			return;
		}

		setMovement(currentTarget, entity);
	}

	private void executeTestAI(EntityRef entity) {
		LocalPlayer localPlayer = CoreRegistry.get(LocalPlayer.class);
		if (localPlayer == null) {
			return;
		}
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		if (!ai.locked) {
			// get targets, break if none
			List<Vector3f> targets = ai.movementTargets;
			List<Vector3f> pathTargets = ai.pathTargets;
			if ((targets == null) || (targets.size() < 1)) {
				return;
			}

			Vector3f currentTarget; // check if currentTarget target is a path
									// or not
			if ((pathTargets != null) && (pathTargets.size() > 0)) {
				currentTarget = pathTargets.get(0);
			} else {
				currentTarget = targets.get(0);
			}
			if (ai.previousTarget != ai.movementTargets.get(0)) {
				ai.locked = true;
				ai.pathTargets = aStarPathing.findPath(worldPos, new Vector3f(
						currentTarget));
				if (ai.pathTargets == null) {
					// MinionSystem minionSystem = new MinionSystem();
					MinionMessage messagetosend = new MinionMessage(
							MinionMessagePriority.Debug, "test", "testdesc",
							"testcont", entity, localPlayer.getEntity());
					//entity.send(new MinionMessageEvent(messagetosend));
					ai.movementTargets.remove(0);
				}
			}
			ai.locked = false;
			if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
				pathTargets = ai.pathTargets;
				ai.previousTarget = targets.get(0); // used to check if the
													// final target changed
				currentTarget = pathTargets.get(0);
			}

			// trying to solve distance calculation with some simple trick of
			// reducing the height to a round int, might not work for taller
			// entities
			worldPos.y = worldPos.y - (worldPos.y % 1) + 0.5f;
			// calc distance to current Target
			Vector3f dist = new Vector3f(worldPos);
			dist.sub(currentTarget);
			double distanceToTarget = dist.length();

			if (distanceToTarget < 0.1d) {
				if ((ai.pathTargets != null) && (ai.pathTargets.size() > 0)) {
					ai.pathTargets.remove(0);
					entity.saveComponent(ai);
				} else {
					if (ai.movementTargets.size() > 0) {
						ai.movementTargets.remove(0);
					}
					ai.previousTarget = null;
					entity.saveComponent(ai);
				}
				return;
			}

			setMovement(currentTarget, entity);
		}
	}

	private void setMovement(Vector3f currentTarget, EntityRef entity) {
		LocationComponent location = entity
				.getComponent(LocationComponent.class);
		SimpleMinionAIComponent ai = entity
				.getComponent(SimpleMinionAIComponent.class);
		CharacterMovementComponent moveComp = entity
				.getComponent(CharacterMovementComponent.class);
		AnimationComponent animcomp = entity
				.getComponent(AnimationComponent.class);
		SkeletalMeshComponent skeletalcomp = entity
				.getComponent(SkeletalMeshComponent.class);
		Vector3f worldPos = new Vector3f(location.getWorldPosition());

		Vector3f dist = new Vector3f(worldPos);
		dist.sub(currentTarget);
		double distanceToTarget = dist.length();

		Vector3f targetDirection = new Vector3f();
		targetDirection.sub(currentTarget, worldPos);
		if (targetDirection.x * targetDirection.x + targetDirection.z
				* targetDirection.z > 0.01f) {
			if (timer.getTimeInMs() - ai.lastDistancecheck > 2000) {
				ai.lastDistancecheck = timer.getTimeInMs();
				if (ai.lastPosition == null) {
					ai.lastPosition = location.getWorldPosition();
				} else if (ai.lastPosition.x == location.getLocalPosition().x
						&& ai.lastPosition.z == location.getWorldPosition().z) {
					// minion has been stuck at same position => teleport
					if (skeletalcomp.animation == animcomp.walkAnim) {
						changeAnimation(entity, animcomp.idleAnim, true);
					}
					location.setWorldPosition(currentTarget);
				} else {
					ai.lastPosition = location.getWorldPosition();
				}
			}
			changeAnimation(entity, animcomp.walkAnim, true);
			targetDirection.normalize();
			moveComp.setDrive(targetDirection);

			float yaw = (float) Math
					.atan2(targetDirection.x, targetDirection.z);
			AxisAngle4f axisAngle = new AxisAngle4f(0, 1, 0, yaw);
			location.getLocalRotation().set(axisAngle);
		} else if (distanceToTarget > 1) {
			// the minion arrived at right x and z but is standing below / above
			// it => teleport
			location.setWorldPosition(currentTarget);
		} else {
			if (skeletalcomp.animation == animcomp.walkAnim) {
				changeAnimation(entity, animcomp.idleAnim, true);
			}
			moveComp.setDrive(new Vector3f());
		}
		entity.saveComponent(ai);
		entity.saveComponent(moveComp);
		entity.saveComponent(location);
	}

	private boolean attack(EntityRef minion, Vector3f position) {

		int damage = 1;
		Block block = worldProvider.getBlock(new Vector3f(position.x,
				position.y - 0.5f, position.z));
		if ((block.isDestructible()) && (block.isTargetable())) {
			EntityRef blockEntity = blockEntityRegistry
					.getOrCreateEntityAt(new Vector3i(position));
			if (blockEntity == EntityRef.NULL) {
				return false;
			} else {
				blockEntity.send(new DamageEvent(damage, minion));
				return true;
			}
		}
		return false;
	}

	@ReceiveEvent(components = { SimpleMinionAIComponent.class })
	public void onBump(HorizontalCollisionEvent event, EntityRef entity) {
		CharacterMovementComponent moveComp = entity
				.getComponent(CharacterMovementComponent.class);
		if ((moveComp != null) && (moveComp.isGrounded)) {
			moveComp.jump = true;
			entity.saveComponent(moveComp);
		}
	}
}
