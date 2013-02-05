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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector2f;

import org.terasology.asset.AssetType;
import org.terasology.asset.AssetUri;
import org.terasology.logic.manager.AudioManager;
import org.terasology.rendering.gui.framework.UIDisplayContainer;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.ChangedListener;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.framework.events.MouseMoveListener;
import org.terasology.rendering.gui.widgets.UILabel;

/**
 * A simple graphical button usable for creating user interface.
 * 
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Marcel Lehwald <marcel.lehwald@googlemail.com>
 * 
 *         TODO program from scratch -> integrate state button here -> implement
 *         radio button?
 */
public class UIModButtonSpecial extends UIDisplayContainer {

	public enum ButtonType {
		Settings,
		Minions,
		Zones,
		Building,
		Crafting,
		Messages,
		MiniMap
	};
	private UILabel hoverlabel;
	private String hovertext;
	private boolean linked = false;
	private boolean mouseover = false;
	private boolean enabled = true;
	private final List<ChangedListener> changedListeners = new ArrayList<ChangedListener>();
	private final Map<String, Vector2f[]> states = new HashMap<String, Vector2f[]>();

	/**
	 * Create a simple button, where 2 types are possible. The normal button and
	 * the toggle button.
	 * 
	 * @param size
	 *            The size of the button.
	 * @param buttonType
	 *            The type of the button which can be normal or toggle.
	 */
	public UIModButtonSpecial(Vector2f size, ButtonType buttontype) {
		setSize(size);

		// default arrow buttons
		setTexture("miniion:specialbuttons");
		switch(buttontype){
			case Settings :{
				setNormalState(new Vector2f(0.0f, 0.0f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 0.0f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 0.0f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 0.0f), new Vector2f(40f, 30f));
				break;
			}
			case Minions :{
				setNormalState(new Vector2f(0.0f, 30f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 30f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 30f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 30f), new Vector2f(40f, 30f));
				break;
			}
			case Zones: {
				setNormalState(new Vector2f(0.0f, 60f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 60f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 60f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 60f), new Vector2f(40f, 30f));
				break;
			}
			case Building : {
				setNormalState(new Vector2f(0.0f, 90f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 90f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 90f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 90f), new Vector2f(40f, 30f));
				break;
			}
			case Crafting : {
				setNormalState(new Vector2f(0.0f, 120f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 120f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 120f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 120f), new Vector2f(40f, 30f));
				break;
			}
			case Messages : {
				setNormalState(new Vector2f(0.0f, 150f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 150f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 150f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 150f), new Vector2f(40f, 30f));
				break;
			}
			default : {
				setNormalState(new Vector2f(0.0f, 210f), new Vector2f(40f, 30f));
				setHoverState(new Vector2f(40f, 210f), new Vector2f(40f, 30f));
				setPressedState(new Vector2f(80f, 210f), new Vector2f(40f, 30f));
				setDisabledState(new Vector2f(120f, 210f), new Vector2f(40f, 30f));
				break;
			} 
		}		
		

		// default state
		if(enabled){
			setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
		}else{
			setBackgroundImage(states.get("disabled")[0], states.get("disabled")[1]);
		}

		addMouseMoveListener(new MouseMoveListener() {
			@Override
			public void leave(UIDisplayElement element) {
				if(enabled){
					setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
					if(hoverlabel != null){
						hoverlabel.setVisible(false);
						/*if(linked){
							hoverlabel.setText("");
						}else{
							hoverlabel.setVisible(false);
						}*/						
					}
					mouseover = false;
				}
			}

			@Override
			public void hover(UIDisplayElement element) {

			}

			@Override
			public void enter(UIDisplayElement element) {
				if(enabled){
					AudioManager.play(new AssetUri(AssetType.SOUND, "engine:click"), 1.0f);
					setBackgroundImage(states.get("hover")[0], states.get("hover")[1]);
					if(hoverlabel != null){
						hoverlabel.setVisible(true);
						/*if(linked){
							hoverlabel.setText(hovertext);
						}else{
							hoverlabel.setVisible(true);
						}*/						
					}
					mouseover = true;
				}
			}

			@Override
			public void move(UIDisplayElement element) {

			}
		});

		addMouseButtonListener(new MouseButtonListener() {
			@Override
			public void up(UIDisplayElement element, int button, boolean intersect) {
				if(enabled){
					if (intersect) {
						setBackgroundImage(states.get("hover")[0],	states.get("hover")[1]);					
					}else{
						setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
					}
				}
			}

			@Override
			public void down(UIDisplayElement element, int button, boolean intersect) {
				if(enabled){
					if (intersect) {
						setBackgroundImage(states.get("pressed")[0], states.get("pressed")[1]);
					}
				}
			}

			@Override
			public void wheel(UIDisplayElement element, int wheel,
					boolean intersect) {

			}
		});

	}
	
	public UIModButtonSpecial(Vector2f size, ButtonType buttontype, Vector2f hoverposition, String Hovertext) {
		this(size,buttontype);
		this.hovertext = hovertext;
		hoverlabel = new UILabel(Hovertext);
		hoverlabel.setPosition(hoverposition);
		hoverlabel.setVisible(false);
		addDisplayElement(hoverlabel);
	}

	public void setColorOffset(int offset, ButtonType buttontype) {
		if(enabled){
			setNormalState(new Vector2f(0.0f, offset), new Vector2f(40f, 30f));
		}else{
			setNormalState(new Vector2f(120f, offset), new Vector2f(40f, 30f));
		}
	}

	/**
	 * Set the texture of the button. Use setNormalTexture, setHoverTexture and
	 * setPressedTexture to configure the texture origin and size of the
	 * different states.
	 * 
	 * @param texture
	 *            The texture to load by the AssetManager.
	 */
	public void setTexture(String texture) {
		setBackgroundImage(texture);
	}
	
	/**
	 * disbaled button can't be clicked
	 * @param enabled enable / disable
	 */
	public void setEnabled(boolean enabled){
		this.enabled = enabled;
		setNormalState(states.get("disabled")[0], states.get("disabled")[1]);
	}	

	/**
	 * Set the normal states texture origin and size. Set the texture by using
	 * setTexture.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setNormalState(Vector2f origin, Vector2f size) {
		if(enabled){
			states.remove("normal");
			states.put("normal", new Vector2f[] { origin, size });
		}else{
			states.remove("disabled");
			states.put("disabled", new Vector2f[] { origin, size });
		}

		// set default state
		if(enabled){
			setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
		}else{
			setBackgroundImage(states.get("disabled")[0], states.get("disabled")[1]);
		}

	}

	/**
	 * Set the hover states texture origin and size. Set the texture by using
	 * setTexture. In toggle mode this texture will be ignored.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setHoverState(Vector2f origin, Vector2f size) {
		states.remove("hover");
		states.put("hover", new Vector2f[] { origin, size });

		// set default state
		if(enabled){
			setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
		}else{
			setBackgroundImage(states.get("disabled")[0], states.get("disabled")[1]);
		}
	}

	/**
	 * Set the pressed states texture origin and size. Set the texture by using
	 * setTexture.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setPressedState(Vector2f origin, Vector2f size) {
		states.remove("pressed");
		states.put("pressed", new Vector2f[] { origin, size });

		// set default state
		if(enabled){
			setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
		}else{
			setBackgroundImage(states.get("disabled")[0], states.get("disabled")[1]);
		}
	}
	
	/**
	 * Set the disabled states texture origin and size. Set the texture by using
	 * setTexture.
	 * 
	 * @param origin
	 *            The origin.
	 * @param size
	 *            The size.
	 */
	public void setDisabledState(Vector2f origin, Vector2f size) {
		states.remove("disabled");
		states.put("disabled", new Vector2f[] { origin, size });

		// set default state
		if(enabled){
			setBackgroundImage(states.get("normal")[0], states.get("normal")[1]);
		}else{
			setBackgroundImage(states.get("disabled")[0], states.get("disabled")[1]);
		}
	}

	public void addChangedListener(ChangedListener listener) {
		changedListeners.add(listener);
	}

	public void removeChangedListener(ChangedListener listener) {
		changedListeners.remove(listener);
	}
	
	public boolean isMouseOver(){
		return mouseover;
	}
	
	public boolean isEnabled(){
		return enabled;
	}
}
