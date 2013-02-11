package org.terasology.miniion.gui;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.controllers.MenuControlSystem;
import org.terasology.components.InventoryComponent;
import org.terasology.craft.events.crafting.RadialCraftEvent;
import org.terasology.entitySystem.EntityRef;
import org.terasology.events.ActivateEvent;
import org.terasology.game.CoreRegistry;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.LocalPlayer;
import org.terasology.logic.manager.GUIManager;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIWindow;


/**
 * a radial menu currently bound to the radial tool item.4
 * @author od
 *
 */
public class UIRadial extends UIWindow{

	private final int iconsize = 72;
	private int slotStart = 0, slotEnd = 20;
	private static final Logger logger = LoggerFactory.getLogger(UIRadial.class);
	private static int level = 0, previouslevel = 0;
	private final UIImage radmid, radinno, radinse, radinsw, radno, radne, radea, radse, radso, radsw, radwe, radnw;
	private final UIImage radmidIcon, radinnoIcon, radinseIcon, radinswIcon, radnoIcon, radneIcon, radeaIcon, radseIcon, radsoIcon, radswIcon, radweIcon, radnwIcon;
	private final UIImage radinnosel, radinsesel, radinswsel, radnosel, radnesel, radeasel, radsesel, radsosel, radswsel, radwesel, radnwsel;
	private List<UIModItemCell> cells = new ArrayList<UIModItemCell>();
	
	 /** The bounds around which we're rendering a menu. */
    protected Rectangle tbounds;

    /** The bounds that all of our menu items occupy. */
    protected Rectangle bounds;
    
    /** Our menu items. */
    protected ArrayList<UIModButtonZodiac> items = new ArrayList<UIModButtonZodiac>();
	
	private MouseButtonListener radialListener = new MouseButtonListener() {

		@Override
		public void wheel(UIDisplayElement element, int wheel,	boolean intersect) {

		}

		@Override
		public void up(UIDisplayElement element, int button, boolean intersect) {
			if (button == 1 || button == 2) {
					// get the radial menu item that was clicked and excute matching action
					int menu = getMenuItem(Mouse.getX(), Mouse.getY());
					// the radial always closes , even if nothing was returned
					executeClick(menu);
			}
		}

		@Override
		public void down(UIDisplayElement element, int button,	boolean intersect) {

		}
	};
	
	public UIRadial(){
		
		setId("activeminiion");
		setModal(true);
		setCloseKeys(new int[] { Keyboard.KEY_ESCAPE });
		
		setSize(new Vector2f(0,0));
		setVerticalAlign(EVerticalAlign.TOP);
		setHorizontalAlign(EHorizontalAlign.LEFT);
		addMouseButtonListener(radialListener);
		setVisible(true);
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
		 * init radial menu items background
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		radmid = new UIImage();
		radmid.setTexture(Assets.getTexture("miniion:radmid"));
		radmid.setSize(new Vector2f(66, 66));
		radmid.setPosition(new Vector2f(Display.getWidth()/2 - 33, Display.getHeight()/2 - 33));
		radmid.setVisible(true);
				
		radinno = new UIImage();
		radinno.setTexture(Assets.getTexture("miniion:radinno"));
		radinno.setSize(new Vector2f(128, 62));
		radinno.setPosition(new Vector2f(Display.getWidth()/2 - 64, Display.getHeight()/2 - 81));
		radinno.setVisible(true);

		radinse = new UIImage();
		radinse.setTexture(Assets.getTexture("miniion:radinse"));
		radinse.setSize(new Vector2f(77, 110));
		radinse.setPosition(new Vector2f(Display.getWidth()/2 + 6, Display.getHeight()/2 - 29));
		radinse.setVisible(true);
		
		radinsw = new UIImage();
		radinsw.setTexture(Assets.getTexture("miniion:radinsw"));
		radinsw.setSize(new Vector2f(77, 110));
		radinsw.setPosition(new Vector2f(Display.getWidth()/2 - 81, Display.getHeight()/2 - 29));
		radinsw.setVisible(false);
		
		radno = new UIImage();
		radno.setTexture(Assets.getTexture("miniion:radno"));
		radno.setSize(new Vector2f(97, 55));
		radno.setPosition(new Vector2f(Display.getWidth()/2 - 49, Display.getHeight()/2 - 139));
		radno.setVisible(true);
		addDisplayElement(radno);
		
		radne = new UIImage();
		radne.setTexture(Assets.getTexture("miniion:radne"));
		radne.setSize(new Vector2f(89, 90));
		radne.setPosition(new Vector2f(Display.getWidth()/2 + 38, Display.getHeight()/2 - 127));
		radne.setVisible(false);
		addDisplayElement(radne);
		
		radea = new UIImage();
		radea.setTexture(Assets.getTexture("miniion:radea"));
		radea.setSize(new Vector2f(55, 97));
		radea.setPosition(new Vector2f(Display.getWidth()/2 + 84, Display.getHeight()/2 - 49));
		radea.setVisible(false);
		addDisplayElement(radea);
		
		radse = new UIImage();
		radse.setTexture(Assets.getTexture("miniion:radse"));
		radse.setSize(new Vector2f(89, 90));
		radse.setPosition(new Vector2f(Display.getWidth()/2 + 37, Display.getHeight()/2 + 37));
		radse.setVisible(true);
		addDisplayElement(radse);
		
		radso = new UIImage();
		radso.setTexture(Assets.getTexture("miniion:radso"));
		radso.setSize(new Vector2f(97, 55));
		radso.setPosition(new Vector2f(Display.getWidth()/2 - 49, Display.getHeight()/2 + 84));
		radso.setVisible(true);
		addDisplayElement(radso);
		
		radsw = new UIImage();
		radsw.setTexture(Assets.getTexture("miniion:radsw"));
		radsw.setSize(new Vector2f(89, 90));
		radsw.setPosition(new Vector2f(Display.getWidth()/2 - 126, Display.getHeight()/2 + 38));
		radsw.setVisible(true);
		addDisplayElement(radsw);
		
		radwe = new UIImage();
		radwe.setTexture(Assets.getTexture("miniion:radwe"));
		radwe.setSize(new Vector2f(55, 97));
		radwe.setPosition(new Vector2f(Display.getWidth()/2 - 139, Display.getHeight()/2 - 49));
		radwe.setVisible(true);
		addDisplayElement(radwe);
		
		radnw = new UIImage();
		radnw.setTexture(Assets.getTexture("miniion:radnw"));
		radnw.setSize(new Vector2f(89, 90));
		radnw.setPosition(new Vector2f(Display.getWidth()/2 - 126, Display.getHeight()/2 - 127));
		radnw.setVisible(true);
		addDisplayElement(radnw);
		
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
		 * menu icons, texture set in switchlevel (shown over radial menu items background)
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		radmidIcon = new UIImage();		
		radmidIcon.setSize(new Vector2f(97, 55));
		radmidIcon.setPosition(new Vector2f(Display.getWidth()/2 - 49, Display.getHeight()/2 - 139));
		radmidIcon.setVisible(false);
		
		radinnoIcon= new UIImage();
		radinnoIcon.setSize(new Vector2f(128, 62));
		radinnoIcon.setPosition(new Vector2f(Display.getWidth()/2 - 64, Display.getHeight()/2 - 81));
		radinnoIcon.setVisible(false);
		
		radinseIcon= new UIImage();
		radinseIcon.setSize(new Vector2f(77, 110));
		radinseIcon.setPosition(new Vector2f(Display.getWidth()/2 + 6, Display.getHeight()/2 - 29));
		radinseIcon.setVisible(false);
		
		radinswIcon= new UIImage();
		radinswIcon.setSize(new Vector2f(77, 110));
		radinswIcon.setPosition(new Vector2f(Display.getWidth()/2 - 81, Display.getHeight()/2 - 29));
		radinswIcon.setVisible(false);
		
		radnoIcon = new UIImage();		
		radnoIcon.setSize(new Vector2f(97, 55));
		radnoIcon.setPosition(new Vector2f(Display.getWidth()/2 - 49, Display.getHeight()/2 - 139));
		radnoIcon.setVisible(false);		
		
		radneIcon = new UIImage();		
		radneIcon.setSize(new Vector2f(89, 90));
		radneIcon.setPosition(new Vector2f(Display.getWidth()/2 + 38, Display.getHeight()/2 - 127));
		radneIcon.setVisible(false);		
		
		radeaIcon = new UIImage();		
		radeaIcon.setSize(new Vector2f(55, 97));
		radeaIcon.setPosition(new Vector2f(Display.getWidth()/2 + 84, Display.getHeight()/2 - 49));
		radeaIcon.setVisible(false);
				
		radseIcon = new UIImage();
		radseIcon.setSize(new Vector2f(89, 90));
		radseIcon.setPosition(new Vector2f(Display.getWidth()/2 + 37, Display.getHeight()/2 + 37));
		radseIcon.setVisible(false);
				
		radsoIcon = new UIImage();		
		radsoIcon.setSize(new Vector2f(97, 55));
		radsoIcon.setPosition(new Vector2f(Display.getWidth()/2 - 49, Display.getHeight()/2 + 84));
		radsoIcon.setVisible(false);
				
		radswIcon = new UIImage();		
		radswIcon.setSize(new Vector2f(89, 90));
		radswIcon.setPosition(new Vector2f(Display.getWidth()/2 - 126, Display.getHeight()/2 + 38));
		radswIcon.setVisible(false);
				
		radweIcon = new UIImage();		
		radweIcon.setSize(new Vector2f(55, 97));
		radweIcon.setPosition(new Vector2f(Display.getWidth()/2 - 139, Display.getHeight()/2 - 49));
		radweIcon.setVisible(false);
				
		radnwIcon = new UIImage();		
		radnwIcon.setSize(new Vector2f(89, 90));
		radnwIcon.setPosition(new Vector2f(Display.getWidth()/2 - 126, Display.getHeight()/2 - 127));
		radnwIcon.setVisible(false);
				
		/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  
		 * init transparent selection indicators around radial menu items background
		 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
		//128,62  //187 169
		radinnosel = new UIImage();
		radinnosel.setTexture(Assets.getTexture("miniion:radinnosel"));
		radinnosel.setSize(new Vector2f(132, 66));
		radinnosel.setPosition(new Vector2f(Display.getWidth()/2 - 66, Display.getHeight()/2 - 83));
		radinnosel.setVisible(false);
		//77 110 //  256 221
		radinsesel = new UIImage();
		radinsesel.setTexture(Assets.getTexture("miniion:radinsesel"));
		radinsesel.setSize(new Vector2f(81, 114));
		radinsesel.setPosition(new Vector2f(Display.getWidth()/2 + 4, Display.getHeight()/2 - 31));
		radinsesel.setVisible(false);
		//77 110		//169 221
		radinswsel = new UIImage();
		radinswsel.setTexture(Assets.getTexture("miniion:radinswsel"));
		radinswsel.setSize(new Vector2f(81, 114));
		radinswsel.setPosition(new Vector2f(Display.getWidth()/2 - 83, Display.getHeight()/2 - 31));
		radinswsel.setVisible(false);		
		
		radnosel = new UIImage();
		radnosel.setTexture(Assets.getTexture("miniion:radnosel"));
		radnosel.setSize(new Vector2f(101, 59));
		radnosel.setPosition(new Vector2f(Display.getWidth()/2 - 51, Display.getHeight()/2 - 141));
		radnosel.setVisible(false);		
		
		radnesel = new UIImage();
		radnesel.setTexture(Assets.getTexture("miniion:radnesel"));
		radnesel.setSize(new Vector2f(93, 94));
		radnesel.setPosition(new Vector2f(Display.getWidth()/2 + 36, Display.getHeight()/2 - 129));
		radnesel.setVisible(false);
		
		radeasel = new UIImage();
		radeasel.setTexture(Assets.getTexture("miniion:radeasel"));
		radeasel.setSize(new Vector2f(59, 101));
		radeasel.setPosition(new Vector2f(Display.getWidth()/2 + 82, Display.getHeight()/2 - 51));
		radeasel.setVisible(false);
		
		radsesel = new UIImage();
		radsesel.setTexture(Assets.getTexture("miniion:radsesel"));
		radsesel.setSize(new Vector2f(94, 94));
		radsesel.setPosition(new Vector2f(Display.getWidth()/2 + 35, Display.getHeight()/2 + 35));
		radsesel.setVisible(false);
		
		radsosel = new UIImage();
		radsosel.setTexture(Assets.getTexture("miniion:radsosel"));
		radsosel.setSize(new Vector2f(101, 59));
		radsosel.setPosition(new Vector2f(Display.getWidth()/2 - 51, Display.getHeight()/2 + 82));
		radsosel.setVisible(false);
		
		radswsel = new UIImage();
		radswsel.setTexture(Assets.getTexture("miniion:radswsel"));
		radswsel.setSize(new Vector2f(94, 93));
		radswsel.setPosition(new Vector2f(Display.getWidth()/2 - 129, Display.getHeight()/2 + 36));
		radswsel.setVisible(false);
		
		radwesel = new UIImage();
		radwesel.setTexture(Assets.getTexture("miniion:radwesel"));
		radwesel.setSize(new Vector2f(59, 101));
		radwesel.setPosition(new Vector2f(Display.getWidth()/2 - 141, Display.getHeight()/2 - 51));
		radwesel.setVisible(false);
		
		radnwsel = new UIImage();
		radnwsel.setTexture(Assets.getTexture("miniion:radnwsel"));
		radnwsel.setSize(new Vector2f(95, 95));
		radnwsel.setPosition(new Vector2f(Display.getWidth()/2 - 130, Display.getHeight()/2 - 130));
		radnwsel.setVisible(false);
		
		addDisplayElement(radmid);
		addDisplayElement(radinno);
		addDisplayElement(radinse);
		addDisplayElement(radinsw);
		addDisplayElement(radno);
		addDisplayElement(radne);
		addDisplayElement(radea);
		addDisplayElement(radse);
		addDisplayElement(radso);
		addDisplayElement(radsw);
		addDisplayElement(radwe);
		addDisplayElement(radnw);
		
		addDisplayElement(radinnosel);
		addDisplayElement(radinsesel);
		addDisplayElement(radinswsel);
		addDisplayElement(radnosel);
		addDisplayElement(radnesel);
		addDisplayElement(radeasel);
		addDisplayElement(radsesel);
		addDisplayElement(radsosel);
		addDisplayElement(radswsel);
		addDisplayElement(radwesel);
		addDisplayElement(radnwsel);
		
		addDisplayElement(radmidIcon);
		addDisplayElement(radinnoIcon);
		addDisplayElement(radinseIcon);
		addDisplayElement(radinswIcon);
		addDisplayElement(radnoIcon);
		addDisplayElement(radneIcon);
		addDisplayElement(radeaIcon);
		addDisplayElement(radseIcon);
		addDisplayElement(radsoIcon);
		addDisplayElement(radswIcon);
		addDisplayElement(radweIcon);
		addDisplayElement(radnwIcon);
	}
	
	@Override
	public void open() {
		setFocus(this);
        setVisible(true);

        getGUIManager().openWindow(this);
        getGUIManager().checkMouseGrabbing();
        //always open the radial at top level with mouse in middle
        Mouse.setCursorPosition(Display.getWidth()/2, Display.getHeight()/2);
        switchlevel(0,false);
	}	
	
	/**
	 * Execute action corresponding to button and level
	 * @param menu
	 * 				The number of the radial menu item that was clicked
	 */
	protected void executeClick(int menu){
		switch(level){
			case 4: {
				if(menu > 99){
					craftlevelClick(menu);
				}
				break;
			}
			case 5 : {
				zonelevelClick(menu);
				break;
			}
			default : {
				toplevelClick(menu);
				break;
			}
		}
		close();
	}
	
	//top level actions
	private void toplevelClick(int menu){
		switch(menu){
			case 0 : {
				CoreRegistry.get(GUIManager.class).openWindow(MenuControlSystem.PAUSE_MENU);				
				break;
			}
			case 1 : {
				CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
				if(cameraTargetSystem.isTargetAvailable()){					
					EntityRef locplayer = CoreRegistry.get(LocalPlayer.class).getEntity();
					cameraTargetSystem.getTarget().send(new ActivateEvent(cameraTargetSystem.getTarget(), locplayer));
				}
				
				break;
			}
			case 2 : {
				switch(previouslevel){
					case 5 : {
						CoreRegistry.get(GUIManager.class).openWindow("zonebook");
						break;
					}
					default : {
						break;
					}
				}
				break;
			}
			case 5:{
				CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
				if(cameraTargetSystem.isTargetAvailable()){					
					EntityRef locplayer = CoreRegistry.get(LocalPlayer.class).getEntity();
					locplayer.send(new RadialCraftEvent(cameraTargetSystem.getTarget(),cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), -1));
				}
				break;
			}
			case 10 : {
				close();
				CoreRegistry.get(GUIManager.class).openWindow("activeminiion");			
				break;
			}
			case 11 : {
				//use item
			}
			case 12 : {
				CoreRegistry.get(GUIManager.class).openWindow(MenuControlSystem.INVENTORY);				
				break;
			}
			default : {
				break;
			}
		}
	}
	
	// zone submenu actions
	private void zonelevelClick(int menu){
		switch(menu){			
		case 5 : {
			CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
			if(cameraTargetSystem.isTargetAvailable()){				
				MinionSystem.endNewSelection(cameraTargetSystem.getTargetBlockPosition());
			}			
			break;
		}
		case 6 : {
			CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
			if(cameraTargetSystem.isTargetAvailable()){				
				MinionSystem.startNewSelection(cameraTargetSystem.getTargetBlockPosition());
			}
			break;
		}
		case 7 : {
			MinionSystem.resetNewSelection();				
			break;
		}
		}
	}
	
	private void craftlevelClick(int menu){
		CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
		if(cameraTargetSystem.isTargetAvailable()){					
			EntityRef locplayer = CoreRegistry.get(LocalPlayer.class).getEntity();				
			locplayer.send(new RadialCraftEvent(cameraTargetSystem.getTarget(),cameraTargetSystem.getHitPosition(), cameraTargetSystem.getHitNormal(), menu - 100));
		}
	}
	
	
	@Override
	public void render() {
		// costy solution to override default mouseover events which cause focus problems
		// also enables selection in certain degrees from middle
		getMenuItem(Mouse.getX(), Mouse.getY());						
		super.render();
		
	}
	
	/**
	 * set the current menu level
	 * and change the layout of the radial to show sub-menus
	 * Should be triggered from within getMenuItem normally and on open 
	 */
	private void switchlevel(int level, boolean setHistory){
		//remember the previous level for the special button
		if(setHistory){
			this.previouslevel = this.level;
		}
		//determine which menu items should be shown depending on level
		this.level = level;
		CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);		
		switch(level){
			case 4 :{
				if(cameraTargetSystem.isTargetAvailable()){
					radmidIcon.setTexture(Assets.getTexture("miniion:radne0"));
					radmidIcon.setSize(new Vector2f(89,90));
					radmidIcon.setPosition(new Vector2f(Display.getWidth()/2 -45, Display.getHeight()/2 - 45));
					radmidIcon.setVisible(true);
					radno.setVisible(false);
					radne.setVisible(false);
					radnesel.setVisible(false);
					radea.setVisible(false);
					radse.setVisible(false);
					radso.setVisible(false);
					radsw.setVisible(false);
					radwe.setVisible(false);
					radnw.setVisible(false);					
					EntityRef locplayer = CoreRegistry.get(LocalPlayer.class).getEntity();
					InventoryComponent entityInventory = locplayer.getComponent(InventoryComponent.class);
					//remove old cells
		            for (UIModItemCell cell : cells) {
		                removeDisplayElement(cell);
		            }
		            cells.clear();
		            
		            //add new cells
		            setVisible(true);

		            int start = 0;
		            int end = entityInventory.itemSlots.size();
		                    
		            if (slotStart != -1) {
		                start = slotStart;
		            }
		            
		            if (slotEnd != -1) {
		            	slotEnd = entityInventory.itemSlots.size();
		                end = slotEnd;
		            }
		            double radstep = Math.PI / 5;
		            double radcounter = 0-Math.PI/2;
		            Vector2f cellSize = new Vector2f(32,32);
		            Vector2f iconPosition = new Vector2f(-7f, -3f);
		            int slotcounter = 0;
		            for (int i = start; i < 10; ++i)
		            {
		            	UIModItemCell cell = new UIModItemCell(locplayer, cellSize, iconPosition);
		                cell.setItemEntity(entityInventory.itemSlots.get(i), i);
		                cell.setSize(cellSize);
		                
		                int posx = (int) (Math.cos(radcounter) * 106) + (Display.getWidth()/2) - 16;
		                //if(posx > Display.getWidth()/2) posx -= 32;
		                //else posx += 32;
		                
		                int posy = (int) (Math.sin(radcounter) * 106) + (Display.getHeight()/2) - 16;
		                //if(posy > Display.getHeight()/2) posy += 16;
		                //else posy -= 16;
		                
		                cell.setPosition(new Vector2f(posx, posy));
		                cell.setVisible(true);
		                radcounter += radstep;
		                cells.add(cell);
		                addDisplayElement(cell);
		            }
		            radstep = Math.PI / ((slotEnd - 10) /2);
		            radcounter = 0 - Math.PI/2;
		            for (int i = 10; i < end; ++i)
		            {
		                UIModItemCell cell = new UIModItemCell(locplayer, cellSize, iconPosition);
		                cell.setItemEntity(entityInventory.itemSlots.get(i), i);
		                cell.setSize(cellSize);
		                
		                int posx = (int) (Math.cos(radcounter) * 180) + (Display.getWidth()/2) - 16;
		                //if(posx > Display.getWidth()/2) posx -= 32;
		                //else posx += 32;
		                
		                int posy = (int) (Math.sin(radcounter) * 180) + (Display.getHeight()/2) - 16;
		                //if(posy > Display.getHeight()/2) posy += 16;
		                //else posy -= 16;
		                
		                cell.setPosition(new Vector2f(posx, posy));
		                cell.setVisible(true);
		                radcounter += radstep;
		                cells.add(cell);
		                addDisplayElement(cell);
		            }
				}
				break;
			}
			case 5 :{
				radmidIcon.setTexture(Assets.getTexture("miniion:radea0"));
				radmidIcon.setSize(new Vector2f(55,97));
				radmidIcon.setPosition(new Vector2f(Display.getWidth()/2 -28, Display.getHeight()/2 - 49));
				radmidIcon.setVisible(true);
				radinsw.setVisible(true);
				radinswIcon.setTexture(Assets.getTexture("miniion:radinsw5"));
				radneIcon.setTexture(Assets.getTexture("miniion:radne5"));
				radeaIcon.setTexture(Assets.getTexture("miniion:radea5"));
				radseIcon.setTexture(Assets.getTexture("miniion:radse5"));
				radno.setVisible(false);
				radso.setVisible(false);
				radsw.setVisible(false);
				radwe.setVisible(false);
				radnw.setVisible(false);
				if(MinionSystem.getNewZone() != null){
					radne.setVisible(true);
					radse.setVisible(true);										
				}else{
					radne.setVisible(false);
					radse.setVisible(false);
				}
				break;
			}
			default : {
				radmidIcon.setVisible(false);
				//experimental button, uses the previous selected submenu
				//to determine content, allowing quick access to a last used item
				switch(previouslevel){
					case 4: {
						//remove old cells
			            for (UIModItemCell cell : cells) {
			                removeDisplayElement(cell);
			            }
			            cells.clear();
					}
					case 5: {
						radinswIcon.setTexture(Assets.getTexture("miniion:radinsw5"));
						radinsw.setVisible(true);
						break;
					}
					default: {
						radinsw.setVisible(false);
						break;
					}
				}
				radinnoIcon.setTexture(Assets.getTexture("miniion:radinno0"));
				radinseIcon.setTexture(Assets.getTexture("miniion:radinse0"));
				radnoIcon.setTexture(Assets.getTexture("miniion:radno0"));
				radneIcon.setTexture(Assets.getTexture("miniion:radne0"));
				radeaIcon.setTexture(Assets.getTexture("miniion:radea0"));
				//radseIcon.setTexture(Assets.getTexture("miniion:radse0"));
				radsoIcon.setTexture(Assets.getTexture("miniion:radso0"));
				//radswIcon.setTexture(Assets.getTexture("miniion:radsw0"));
				radweIcon.setTexture(Assets.getTexture("miniion:radwe0"));
				radnwIcon.setTexture(Assets.getTexture("miniion:radnw0"));
				radno.setVisible(true);
				radso.setVisible(false);
				radse.setVisible(false);
				radsw.setVisible(false);
				radwe.setVisible(true);
				radnw.setVisible(false);
				if(cameraTargetSystem.isTargetAvailable()){
					radne.setVisible(true);
					radea.setVisible(true);
				}else{
					radne.setVisible(false);
					radea.setVisible(false);
				}
				break;
			}
		}
		//set matching visibility for the icons on the buttons
		if(radinno.isVisible()) radinnoIcon.setVisible(true);
		else radinnoIcon.setVisible(false);
		if(radinse.isVisible()) radinseIcon.setVisible(true);
		else radinseIcon.setVisible(false);
		if(radinsw.isVisible()) radinswIcon.setVisible(true);
		else radinswIcon.setVisible(false);
		if(radne.isVisible()) radneIcon.setVisible(true);
		else radneIcon.setVisible(false);
		if(radea.isVisible()) radeaIcon.setVisible(true);
		else radeaIcon.setVisible(false);
		if(radse.isVisible()) radseIcon.setVisible(true);
		else radseIcon.setVisible(false);
		if(radso.isVisible()) radsoIcon.setVisible(true);
		else radsoIcon.setVisible(false);
		if(radsw.isVisible()) radswIcon.setVisible(true);
		else radswIcon.setVisible(false);
		if(radwe.isVisible()) radweIcon.setVisible(true);
		else radweIcon.setVisible(false);
		if(radnw.isVisible()) radnwIcon.setVisible(true);
		else radnwIcon.setVisible(false);
		if(radno.isVisible()) radnoIcon.setVisible(true);
		else radnoIcon.setVisible(false);
	}
	
	/**
	 * Check if a radial menu item is currently selected
	 * return the number of the selected menu
	 * and set submenu level if needed 
	 * @param mousex
	 * 				the current x position if the mouse
	 * @param mousey
	 * 				the current y position if the mouse
	 * @return
	 * 				the number of the radial menu item that was selected or -1 if none
	 */
	private int getMenuItem(int mousex, int mousey){
		Vector2f startpoint = new Vector2f(Display.getWidth()/2, Display.getHeight()/2);
		startpoint.sub(new Vector2f(mousex, mousey));
		double distance = startpoint.length();
		
		//middle of radial
		if(distance < 34){
			if(level != 0){
				switchlevel(0, true);
			}
		}
		radinnosel.setVisible(false);
		radinsesel.setVisible(false);
		radinswsel.setVisible(false);
		// inner 3 buttons
		if(distance < 80 && distance > 33){
			int rad = (int) Math.toDegrees(Math.atan2(mousex - Display.getWidth()/2, mousey - Display.getHeight()/2 ));
			if(rad < 0){
				rad += 360;
			}
			//could calculate gaps and return here, not sure if it would be better though
			if(310 < rad  || 50 > rad ){
				if(radinno.isVisible()){
					radinnosel.setVisible(true);
				}
				return 0;
			}
			if(70 < rad  && 170 > rad ){
				if(radinse.isVisible()){
					radinsesel.setVisible(true);
				}
				return 1;
			}
			if(190 < rad  && 290 > rad ){
				if(radinsw.isVisible()){
					radinswsel.setVisible(true);
				}
				return 2;
			}
		}
		if(level == 4){
			if(distance < 123 && distance > 89){
				float rad = (float) Math.toDegrees(Math.atan2(mousex - Display.getWidth()/2, mousey - Display.getHeight()/2 ));
				if(rad < 0){
					rad += 360;
					//need to catch half of the icon that overlaps -pi <> pi
					if(rad > 354){
						return 100;
					}
				}
				//check for click on gap, in this case 1/4th of div
				int modulus = (int) (rad % 36); 
				if(9 > modulus  || modulus > 27 ){
					return (int) Math.round(rad /36) + 100;
				}
				return  -1;
			}
			if(distance < 197 && distance > 165){
				float rad = (float) Math.toDegrees(Math.atan2(mousex - Display.getWidth()/2, mousey - Display.getHeight()/2 ));
				if(rad < 0){
					rad += 360;
					//need to catch half of the icon that overlaps -pi <> pi
					if(rad > 354){
						return 100;
					}
				}
				//no real need to check for gaps, items are close enough
				return  (int) Math.round(rad /12) + 110;				
			}
			return -1;
		}
		radnesel.setVisible(false);
		radeasel.setVisible(false);
		radsesel.setVisible(false);
		radsosel.setVisible(false);
		radswsel.setVisible(false);
		radwesel.setVisible(false);
		radnwsel.setVisible(false);
		radnosel.setVisible(false);
		// the first ring level
		if(distance < 139 && distance > 88){
			int rad = (int) Math.toDegrees(Math.atan2(mousex - Display.getWidth()/2, mousey - Display.getHeight()/2 ));
			if(rad < 0){
				rad += 360;
			}
			if(25 < rad  && 65 > rad ){
				if(radne.isVisible()){
					radnesel.setVisible(true);
					if(level == 0){
						switchlevel(4, true);
					}
				}				
				return 5;
			}
			if(70 < rad  && 110 > rad ){
				if(radea.isVisible()){
					radeasel.setVisible(true);
					if(level == 0){
						switchlevel(5, true);
					}
				}
				return 6;
			}
			if(115 < rad  && 155 > rad ){
				if(radse.isVisible()){
					radsesel.setVisible(true);
				}
				return 7;
			}
			if(160 < rad  && 200 > rad ){
				if(radso.isVisible()){
					radsosel.setVisible(true);
				}
				return 8;
			}
			if(205 < rad  && 245 > rad ){
				if(radsw.isVisible()){
					radswsel.setVisible(true);
				}
				return 9;
			}
			if(250 < rad  && 290 > rad ){
				if(radwe.isVisible()){
					radwesel.setVisible(true);
				}
				return 10;
			}
			if(295 < rad  && 335 > rad ){
				if(radnw.isVisible()){
					radnwsel.setVisible(true);
				}
				return 11;
			}
			if(340 < rad  || 20 > rad ){
				if(radno.isVisible()){
					radnosel.setVisible(true);
				}
				return 12;
			}
		}				
		return -1;
	}
}
