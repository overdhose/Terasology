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
package org.terasology.miniion.utilities;

import org.terasology.game.CoreRegistry;
import org.terasology.math.Vector3i;
import org.terasology.miniion.minionenum.ZoneType;
import org.terasology.world.WorldProvider;

public class Zone {

	protected WorldProvider worldProvider;

	protected Vector3i minbounds = new Vector3i(Integer.MAX_VALUE,
			Integer.MAX_VALUE, Integer.MAX_VALUE);
	protected Vector3i maxbounds = new Vector3i(Integer.MIN_VALUE,
			Integer.MIN_VALUE, Integer.MIN_VALUE);

	/* CONST */
	private static final int maxselectionbounds = 50;	
    
    
	protected Vector3i startposition, endposition;
	//used by terraforming and building
	//true when the corresponding action is finished.
	private boolean actioncomplete = false; 
	//used to undo zones with unbreakable blocks
	//zone set to delete untill blocks are removed
	private boolean deleted = false;

	public String Name;
	public ZoneType zonetype;
	public int zoneheight;
	public int zonedepth;
	public int zonewidth;

	public Zone() {
	}

	public Zone(Vector3i startposition, Vector3i endposition) {
		this();
		this.startposition = startposition;
		this.endposition = endposition;
		calcBounds(startposition);
		if(endposition != null){
			calcBounds(endposition);
		}
	}

	private void calcBounds(Vector3i gridPosition) {
		if (gridPosition.x < minbounds.x) {
			minbounds.x = gridPosition.x;
		}
		if (gridPosition.y < minbounds.y) {
			minbounds.y = gridPosition.y;
		}
		if (gridPosition.z < minbounds.z) {
			minbounds.z = gridPosition.z;
		}

		if (gridPosition.x > maxbounds.x) {
			maxbounds.x = gridPosition.x;
		}
		if (gridPosition.y > maxbounds.y) {
			maxbounds.y = gridPosition.y;
		}
		if (gridPosition.z > maxbounds.z) {
			maxbounds.z = gridPosition.z;
		}
	}

	public void setStartPosition(Vector3i startpos) {
		startposition = startpos;
		calcBounds(startposition);
	}

	public Vector3i getStartPosition() {
		return startposition;
	}

	public void setEndPosition(Vector3i endpos) {
		endposition = endpos;
		calcBounds(endposition);
	}

	public Vector3i getEndPosition() {
		return endposition;
	}

	public Vector3i getMinBounds() {
		return minbounds;
	}

	public Vector3i getMaxBounds() {
		return maxbounds;
	}
	
	public boolean isActionComplete(){
		return actioncomplete;
	}
	
	public void setActionComplete(){
		actioncomplete = true;
	}

	public boolean outofboundselection(){
		boolean retval = false;
		if(startposition != null && endposition != null){
			if(getAbsoluteDiff(minbounds.x, maxbounds.x) > maxselectionbounds){
				retval = true;
			}
			if(getAbsoluteDiff(minbounds.y, maxbounds.y) > maxselectionbounds){
				retval = true;
			}
			if(getAbsoluteDiff(minbounds.z, maxbounds.z) > maxselectionbounds){
				retval = true;
			}
		}
		return retval;
	}
	
	public boolean checkFarm(boolean fullgrown){
		if(this.zonetype != ZoneType.OreonFarm){
			return false;
		}
		worldProvider = CoreRegistry.get(WorldProvider.class);
		int y = getMaxBounds().y + 1;
		for (int x = getMinBounds().x; x <= getMaxBounds().x; x++) {
			for (int z = getMinBounds().z; z <= getMaxBounds().z; z++) {
				if(fullgrown){
					if(worldProvider.getBlock(x, y, z).getURI().getFamily().matches("OreonPlant6")){
						return true;
					}
				}
				else if(worldProvider.getBlock(x, y, z).getURI().getFamily().matches("air")){
					return true;
				}					
			}
		}
		return false;
	}

	private int getAbsoluteDiff(int val1, int val2) {
		int width;
		if (val1 == val2) {
			width = 1;
		} else if (val1 < 0) {
			if (val2 < 0 && val2 < val1) {
				width = Math.abs(val2) - Math.abs(val1);
			} else if (val2 < 0 && val2 > val1) {
				width = Math.abs(val1) - Math.abs(val2);
			} else {
				width = Math.abs(val1) + val2;
			}
			width++;
		} else {
			if (val2 > -1 && val2 < val1) {
				width = val1 - val2;
			} else if (val2 > -1 && val2 > val1) {
				width = val2 - val1;
			} else {
				width = Math.abs(val2) + val1;
			}
			width++;
		}
		return width;
	}

	
}