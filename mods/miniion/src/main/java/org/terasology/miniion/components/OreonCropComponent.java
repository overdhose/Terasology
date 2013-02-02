package org.terasology.miniion.components;

import org.terasology.entitySystem.Component;

public class OreonCropComponent implements Component{
	
	private boolean fullgrown = false;
	private byte pickstage;
	private boolean busy = false;
	
	public long lastgrowthcheck;
	public byte stages;	
		
	public boolean isFullgrown(){
		return fullgrown;
	}
	
	public byte pickCrop(){
		fullgrown = false;
		return pickstage;
	}
	
	public void setFullGrown(){
		fullgrown = true;
	}
	
	public boolean setBusy(boolean busy){
		if(this.busy != busy){
			this.busy = busy;
		}
		return this.busy;
	}
}
