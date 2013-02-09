package org.terasology.miniion.utilities;

import static org.lwjgl.opengl.GL11.glColorMask;

import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import org.lwjgl.opengl.GL11;
import org.terasology.game.CoreRegistry;
import org.terasology.logic.manager.ShaderManager;
import org.terasology.math.Vector3i;
import org.terasology.miniion.components.MiniionSettingsComponent;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.primitives.Mesh;
import org.terasology.rendering.primitives.Tessellator;
import org.terasology.rendering.primitives.TessellatorHelper;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

public class ZoneWithRender extends Zone{

	private final Mesh mesh;
	
	public ZoneWithRender(){
		Tessellator tessellator = new Tessellator();
        TessellatorHelper.addBlockMesh(tessellator, new Vector4f(0.0f, 0.0f, 1.0f, 0.25f), 1.005f, 1.0f, 0.5f, 0.0f, 0.0f, 0.0f);
        mesh = tessellator.generateMesh();
	}
	
	public void render() {
		if(MinionSystem.getNewZone() != null && this.equals(MinionSystem.getNewZone())){
			if(MinionSystem.getSettings() == null) return;
	        ShaderManager.getInstance().enableDefault();
	        worldProvider = CoreRegistry.get(WorldProvider.class);

	        for (int i = 0; i < 2; i++) {
	            if (i == 0) {
	                glColorMask(false, false, false, false);
	            } else {
	                glColorMask(true, true, true, true);
	            }
	            Vector3f cameraPosition = CoreRegistry.get(WorldRenderer.class).getActiveCamera().getPosition();
	            int camx = (int)cameraPosition.x;
	            int camy = (int)cameraPosition.y;
	            int camz = (int)cameraPosition.z;
	            if(MinionSystem.getNewZone().startposition != null){
	            	Vector3i renderpos = MinionSystem.getNewZone().startposition;
	            	GL11.glPushMatrix();
	            	GL11.glTranslated(renderpos.x - cameraPosition.x, renderpos.y - cameraPosition.y, renderpos.z - cameraPosition.z);
	            	mesh.render();
	            	GL11.glPopMatrix();
	            	if(MinionSystem.getNewZone().endposition != null){
	            		renderpos = MinionSystem.getNewZone().endposition;
	            		GL11.glPushMatrix();
		            	GL11.glTranslated(renderpos.x - cameraPosition.x, renderpos.y - cameraPosition.y, renderpos.z - cameraPosition.z);
		            	mesh.render();
		            	GL11.glPopMatrix();
		            	MiniionSettingsComponent settingcomp = MinionSystem.getSettings().getComponent(MiniionSettingsComponent.class);
		            	if(!outofboundselection() && settingcomp.showSelection){
			            	for (int x = getMinBounds().x; x <= getMaxBounds().x; x++) {
				    			for (int z = getMinBounds().z; z <= getMaxBounds().z; z++) {
				    				for (int y = getMaxBounds().y; y >= getMinBounds().y; y--) {
				    					Block tmpblock;
				    					if(worldProvider.getBlock(x - camx, y - camy, z- camz) == null){
				    						continue;
				    					}else{
				    						tmpblock = worldProvider.getBlock(x, y, z);
				    					} //!tmpblock.getBlockFamily().getURI().getFamily().matches("air")
				    					if (!tmpblock.isInvisible()) {
				    						if(x==minbounds.x || x == maxbounds.x){
				    							GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
				    						}else
				    						if(z==minbounds.z || z == maxbounds.z){
				    							GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
				    						}else{
					    						GL11.glPushMatrix();
					    		            	GL11.glTranslated(x - cameraPosition.x, y - cameraPosition.y, z - cameraPosition.z);
					    		            	mesh.render();
					    		            	GL11.glPopMatrix();
					    						break;
				    						}
				    					}
				    				}
				    			}
				    		}
		            	}
	            	}
	            }	           
	        }
		}
    }
}
