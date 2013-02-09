package org.terasology.miniion.gui;

import java.awt.Rectangle;
import java.util.ArrayList;

import javax.vecmath.Vector2f;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.asset.Assets;
import org.terasology.componentSystem.controllers.MenuControlSystem;
import org.terasology.game.CoreRegistry;
import org.terasology.input.CameraTargetSystem;
import org.terasology.logic.manager.GUIManager;
import org.terasology.miniion.componentsystem.controllers.MinionSystem;
import org.terasology.rendering.gui.framework.UIDisplayElement;
import org.terasology.rendering.gui.framework.events.MouseButtonListener;
import org.terasology.rendering.gui.widgets.UIImage;
import org.terasology.rendering.gui.widgets.UIWindow;

import static org.lwjgl.opengl.GL11.*;

public class UIRadial extends UIWindow{

	private final int iconsize = 72;
	private static final Logger logger = LoggerFactory.getLogger(UIRadial.class);
	private final UIImage radno, radne, radea, radse, radso, radsw, radwe, radnw;
	private final UIImage radnosel, radnesel, radeasel, radsesel, radsosel, radswsel, radwesel, radnwsel;
	//private final UIModButtonZodiac zod0, zod1, zod2, zod3, zod4, zod5, zod6, zod7, zod8, zod9, zod10, zod11, zod12;
	 /** The bounds around which we're rendering a menu. */
    protected Rectangle tbounds;

    /** The bounds that all of our menu items occupy. */
    protected Rectangle bounds;
    
    /** Our menu items. */
    protected ArrayList<UIModButtonZodiac> items = new ArrayList<UIModButtonZodiac>();
	
	private MouseButtonListener radialListener = new MouseButtonListener() {

		@Override
		public void wheel(UIDisplayElement element, int wheel,
				boolean intersect) {

		}

		@Override
		public void up(UIDisplayElement element, int button,
				boolean intersect) {
			if (button == 1) {
				int menu = getMenuItem(Mouse.getX(), Mouse.getY());
				executeClick(menu);
			}
		}

		@Override
		public void down(UIDisplayElement element, int button,
				boolean intersect) {

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
		/*
		backgroundmain = new UIImage();
		backgroundmain.setTexture(Assets.getTexture("miniion:radialmain"));
		backgroundmain.setSize(new Vector2f(500, 500));
		backgroundmain.setPosition(new Vector2f(50, 50));
		backgroundmain.setVisible(true);
		addDisplayElement(backgroundmain);*/
		/*
		zod0  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.balance);
		zod0.setId("zod0");
		zod0.setVisible(true);		
		zod1  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.cancer);
		zod1.setId("zod1");
		zod1.setVisible(true);		
		zod2  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.scorpio);
		zod2.setId("zod2");
		zod2.setVisible(true);		
		zod3  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.sagitarius);
		zod3.setId("zod3");
		zod3.setVisible(true);		
		zod4  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.taurus);
		zod4.setId("zod4");
		zod4.setVisible(true);		
		zod5  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.capricorn);
		zod5.setId("zod5");
		zod5.setVisible(true);		
		zod6  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.leo);
		zod6.setId("zod6");
		zod6.setVisible(true);		
		zod7  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.aquarius);
		zod7.setId("zod7");
		zod7.setVisible(true);		
		zod8  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.ram);
		zod8.setId("zod8");
		zod8.setVisible(true);		
		zod9  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.gemini);
		zod9.setId("zod9");
		zod9.setVisible(true);		
		zod10  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.virgo);
		zod10.setId("zod10");
		zod10.setVisible(true);		
		zod11  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.pisces);
		zod11.setId("zod11");
		zod11.setVisible(true);
		//13th middle invent icon
		zod12  = new UIModButtonZodiac(new Vector2f(iconsize, iconsize), UIModButtonZodiac.ButtonType.chest);
		zod12.setId("zod12");
		zod12.addMouseButtonListener(radialListener);
		zod12.setPosition(new Vector2f(264,264));
		zod12.setVisible(true);*/
		
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
		
		//selection auras
		radnosel = new UIImage();
		radnosel.setTexture(Assets.getTexture("miniion:radnosel"));
		radnosel.setSize(new Vector2f(101, 59));
		radnosel.setPosition(new Vector2f(Display.getWidth()/2 - 51, Display.getHeight()/2 - 141));
		radnosel.setVisible(false);
		addDisplayElement(radnosel);
		
		radnesel = new UIImage();
		radnesel.setTexture(Assets.getTexture("miniion:radnesel"));
		radnesel.setSize(new Vector2f(93, 94));
		radnesel.setPosition(new Vector2f(Display.getWidth()/2 + 36, Display.getHeight()/2 - 129));
		radnesel.setVisible(false);
		addDisplayElement(radnesel);
		
		radeasel = new UIImage();
		radeasel.setTexture(Assets.getTexture("miniion:radeasel"));
		radeasel.setSize(new Vector2f(59, 101));
		radeasel.setPosition(new Vector2f(Display.getWidth()/2 + 82, Display.getHeight()/2 - 51));
		radeasel.setVisible(false);
		addDisplayElement(radeasel);
		
		radsesel = new UIImage();
		radsesel.setTexture(Assets.getTexture("miniion:radsesel"));
		radsesel.setSize(new Vector2f(94, 94));
		radsesel.setPosition(new Vector2f(Display.getWidth()/2 + 35, Display.getHeight()/2 + 35));
		radsesel.setVisible(false);
		addDisplayElement(radsesel);
		
		radsosel = new UIImage();
		radsosel.setTexture(Assets.getTexture("miniion:radsosel"));
		radsosel.setSize(new Vector2f(101, 59));
		radsosel.setPosition(new Vector2f(Display.getWidth()/2 - 51, Display.getHeight()/2 + 82));
		radsosel.setVisible(false);
		addDisplayElement(radsosel);
		
		radswsel = new UIImage();
		radswsel.setTexture(Assets.getTexture("miniion:radswsel"));
		radswsel.setSize(new Vector2f(94, 93));
		radswsel.setPosition(new Vector2f(Display.getWidth()/2 - 129, Display.getHeight()/2 + 36));
		radswsel.setVisible(false);
		addDisplayElement(radswsel);
		
		radwesel = new UIImage();
		radwesel.setTexture(Assets.getTexture("miniion:radwesel"));
		radwesel.setSize(new Vector2f(59, 101));
		radwesel.setPosition(new Vector2f(Display.getWidth()/2 - 141, Display.getHeight()/2 - 51));
		radwesel.setVisible(false);
		addDisplayElement(radwesel);
		
		radnwsel = new UIImage();
		radnwsel.setTexture(Assets.getTexture("miniion:radnwsel"));
		radnwsel.setSize(new Vector2f(95, 95));
		radnwsel.setPosition(new Vector2f(Display.getWidth()/2 - 130, Display.getHeight()/2 - 130));
		radnwsel.setVisible(false);
		addDisplayElement(radnwsel);
		
		/*items.add(0,zod0);
		items.add(1,zod1);
		items.add(2,zod2);
		items.add(3,zod3);
		items.add(4,zod4);
		items.add(5,zod5);
		items.add(6,zod6);
		items.add(7,zod7);
		items.add(8,zod8);
		items.add(9,zod9);
		items.add(10,zod10);
		items.add(11,zod11);		
		
		double radius = 250f;
		//leave a gap of so that iconsize + gap = 131
		//to go around full circle. adjust according if more / less entries. 
		double padding = 59;
        double theta = (iconsize + padding) / radius ;
        double angle = -Math.PI/2;
		for(int x = 0; x < 12; x++){
			UIModButtonZodiac tmpBtn = items.get(x);
			tmpBtn.addMouseButtonListener(radialListener);
			int ix = (int)(radius * Math.cos(angle));
            int iy = (int)(radius * Math.sin(angle));
            tmpBtn.setPosition(new Vector2f( (int)((ix - (iconsize + padding)/2) + this.getSize().x/2 +20), (iy - iconsize/2) + this.getSize().y/2));
            // move along the circle
            angle += theta;            
		}*/
		
		/*addDisplayElement(zod0);
		addDisplayElement(zod1);
		addDisplayElement(zod2);
		addDisplayElement(zod3);
		addDisplayElement(zod4);
		addDisplayElement(zod5);
		addDisplayElement(zod6);
		addDisplayElement(zod7);
		addDisplayElement(zod8);
		addDisplayElement(zod9);
		addDisplayElement(zod10);
		addDisplayElement(zod11);
		addDisplayElement(zod12);*/
		
		addDisplayElement(radno);
		addDisplayElement(radne);
		addDisplayElement(radea);
		addDisplayElement(radse);
		addDisplayElement(radso);
		addDisplayElement(radsw);
		addDisplayElement(radwe);
		addDisplayElement(radnw);
		
		addDisplayElement(radnosel);
		addDisplayElement(radnesel);
		addDisplayElement(radeasel);
		addDisplayElement(radsesel);
		addDisplayElement(radsosel);
		addDisplayElement(radswsel);
		addDisplayElement(radwesel);
		addDisplayElement(radnwsel);
	}
	
	@Override
	public void open() {
		//super.open();
		setFocus(this);
        setVisible(true);

        getGUIManager().openWindow(this);
        getGUIManager().checkMouseGrabbing();
		//radiallayout();
	}
	
	protected void radiallayout(){
		
		radnesel.setVisible(false);
		radeasel.setVisible(false);
		radsesel.setVisible(false);
		radsosel.setVisible(false);
		radswsel.setVisible(false);
		radwesel.setVisible(false);
		radnwsel.setVisible(false);
		radnosel.setVisible(false);
		
		getMenuItem(Mouse.getX(), Mouse.getY());
		CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
		if(cameraTargetSystem.isTargetAvailable()){
			if(MinionSystem.getNewZone() != null){
				radsesel.setVisible(true);
				if(MinionSystem.getNewZone().getStartPosition() == null){
					radso.setVisible(false);
				}else{
					radso.setVisible(true);
				}
			}else{
				radse.setVisible(true);
				radso.setVisible(false);
			}
		}else{
			radse.setVisible(false);
			radso.setVisible(false);
		}

	}
	
	protected void executeClick(int menu){
		switch(menu){			
			case 7 : {
				CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
				if(cameraTargetSystem.isTargetAvailable()){				
					MinionSystem.startNewSelection(cameraTargetSystem.getTargetBlockPosition());
				}
				break;
			}
			case 8 : {
				CameraTargetSystem cameraTargetSystem  = CoreRegistry.get(CameraTargetSystem.class);
				if(cameraTargetSystem.isTargetAvailable()){				
					MinionSystem.endNewSelection(cameraTargetSystem.getTargetBlockPosition());
				}
				break;
			}
			case 9 : {
				MinionSystem.resetNewSelection();				
				break;
			}
			case 10 : {
				close();
				CoreRegistry.get(GUIManager.class).openWindow("activeminiion");			
				break;
			}
			case 11 : {
				CoreRegistry.get(GUIManager.class).openWindow(MenuControlSystem.PAUSE_MENU);				
				break;
			}
			case 12 : {
				CoreRegistry.get(GUIManager.class).openWindow(MenuControlSystem.INVENTORY);				
				break;
			}
			default : {
				break;
			}
		}
		close();
	}
	
	@Override
	public void render() {
		radiallayout();
		super.render();
		
	}
	
	private int getMenuItem(int mousex, int mousey){
		Vector2f startpoint = new Vector2f(Display.getWidth()/2, Display.getHeight()/2);
		startpoint.sub(new Vector2f(mousex, mousey));
		double distance = startpoint.length();			
		if(distance < 139 && distance > 88){
			int rad = (int) Math.toDegrees(Math.atan2(mousex - Display.getWidth()/2, mousey - Display.getHeight()/2 ));
			if(rad < 0){
				rad += 360;
			}
			if(25 < rad  && 65 > rad ){
				if(radne.isVisible()){
					radnesel.setVisible(true);
				}
				return 5;
			}
			if(70 < rad  && 110 > rad ){
				if(radea.isVisible()){
					radeasel.setVisible(true);
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
