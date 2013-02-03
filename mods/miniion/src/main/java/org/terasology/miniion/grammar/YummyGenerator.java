package org.terasology.miniion.grammar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3f;

import org.terasology.game.CoreRegistry;
import org.terasology.grammarSystem.logic.grammar.ProductionSystem;
import org.terasology.grammarSystem.logic.grammar.assets.Grammar;
import org.terasology.grammarSystem.logic.grammar.shapes.Shape;
import org.terasology.grammarSystem.logic.grammar.shapes.ShapeSymbol;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.DivideArg;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.DivideRule;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SetRule;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.Size;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SplitArg;
import org.terasology.grammarSystem.logic.grammar.shapes.complex.SplitRule;
import org.terasology.grammarSystem.world.building.BuildingGenerator;
import org.terasology.logic.commands.Command;
import org.terasology.logic.commands.CommandParam;
import org.terasology.logic.commands.CommandProvider;
import org.terasology.logic.commands.Commands;
import org.terasology.logic.manager.MessageManager;
import org.terasology.model.structures.BlockCollection;
import org.terasology.model.structures.BlockPosition;
import org.terasology.rendering.cameras.Camera;
import org.terasology.rendering.world.WorldRenderer;
import org.terasology.world.WorldProvider;


/**
 * grammar playing groud
 * @author od
 *
 */
public class YummyGenerator implements CommandProvider{
	
	
	  @Command(shortDescription = "Place a building with specified size in front of the player (of the yummy variety)",
	            helpText = "Places a constructed building in front of the player. For construction, " +
	                    	"a predefined org.terasology.logic.grammar is used. The bounding box of the generated structure is given by the " +
	                    	"specified arguments.")
    public void buildyummy(@CommandParam(name = "width") int width, @CommandParam(name = "height") int height, 
    					@CommandParam(name = "depth") int depth, @CommandParam(name = "index") int index) {
		Camera camera = CoreRegistry.get(WorldRenderer.class).getActiveCamera();
		Vector3f attachPos = camera.getPosition();
		Vector3f offset = camera.getViewingDirection();
		
		offset.scale(3);
		attachPos.add(offset);
		
		BlockCollection collection = getYummyStructure(width, height, depth, index);
		collection.setAttachPos(new BlockPosition(0, 0, 0));
		
		WorldProvider provider = CoreRegistry.get(WorldProvider.class);
		if (provider != null) {
		    collection.build(provider, new BlockPosition(attachPos));
		}
	}
	
	public BlockCollection getYummyStructure(int width, int height, int depth, int index){
		        
        BuildingGenerator generator = complexBuildingGenerator(index);
        BlockCollection collection = generator.generate(width, height, depth);
        return collection;
	}
	
	 public static BuildingGenerator complexBuildingGenerator(int index) {
		 List<String> blocks = getBlocks(index);
		//================================================
		ShapeSymbol house = new ShapeSymbol("house");
		ShapeSymbol ground_floor = new ShapeSymbol("ground_floor");
		ShapeSymbol wall = new ShapeSymbol("wall");
		ShapeSymbol wall_inner = new ShapeSymbol("wall_inner");
		ShapeSymbol win_wall = new ShapeSymbol("win_wall");
		ShapeSymbol door = new ShapeSymbol("door");
		ShapeSymbol border = new ShapeSymbol("border");
		ShapeSymbol middle_part = new ShapeSymbol("middle_part");
		ShapeSymbol middle_part_walls = new ShapeSymbol("middle_part_walls");
		//================================================
		/*SetRule setStone = new SetRule(Commands.resolveBlockUri("stone").get(0));
		SetRule setCobblestone = new SetRule(Commands.resolveBlockUri("cobblestone").get(0));
		SetRule setOaktrunk = new SetRule(Commands.resolveBlockUri("oaktrunk").get(0));
		SetRule setGlass = new SetRule(Commands.resolveBlockUri("glass").get(0));
		SetRule setPlank = new SetRule(Commands.resolveBlockUri("plank").get(0));
		SetRule setAir = new SetRule(Commands.resolveBlockUri("air").get(0));*/
		SetRule setStone = new SetRule(Commands.resolveBlockUri(blocks.get(0)).get(0));
		SetRule setCobblestone = new SetRule(Commands.resolveBlockUri(blocks.get(1)).get(0));
		SetRule setOaktrunk = new SetRule(Commands.resolveBlockUri(blocks.get(2)).get(0));
		SetRule setGlass = new SetRule(Commands.resolveBlockUri(blocks.get(3)).get(0));
		SetRule setPlank = new SetRule(Commands.resolveBlockUri(blocks.get(4)).get(0));
		SetRule setAir = new SetRule(Commands.resolveBlockUri(blocks.get(5)).get(0));
		//================================================
		DivideArg divideHouseGroundFloorArg = new DivideArg(new Size(3f, true), ground_floor);
		DivideArg divideHouseBorderArg = new DivideArg(new Size(1f, true), border);
		DivideArg divideHouseMiddlePartArg = new DivideArg(new Size(1f, false), middle_part);
		DivideArg divideHouseRoofArg = new DivideArg(new Size(1f, true), setPlank.clone());
		LinkedList<DivideArg> divideHouseArgs = new LinkedList<DivideArg>();
		divideHouseArgs.add(divideHouseGroundFloorArg);
		divideHouseArgs.add(divideHouseBorderArg);
		divideHouseArgs.add(divideHouseMiddlePartArg);
		divideHouseArgs.add(divideHouseRoofArg);
		DivideRule divideHouse = new DivideRule(divideHouseArgs, DivideRule.Direction.Y);
		
		DivideArg divideWallStone1Arg = new DivideArg(new Size(1f, true), setStone.clone());
		DivideArg divideWallInnerArg = new DivideArg(new Size(1f, false), wall_inner);
		DivideArg divideWallStone2Arg = new DivideArg(new Size(1f, true), setStone.clone());
		LinkedList<DivideArg> divideWallArgs = new LinkedList<DivideArg>();
		divideWallArgs.add(divideWallStone1Arg);
		divideWallArgs.add(divideWallInnerArg);
		divideWallArgs.add(divideWallStone2Arg);
		DivideRule divideWall = new DivideRule(divideWallArgs, DivideRule.Direction.X);
		
		DivideArg divideWallInnerCobblestoneArg = new DivideArg(new Size(.3f, false), setCobblestone.clone());
		DivideArg divideWallInnerDoorArg = new DivideArg(new Size(1f, true), door);
		DivideArg divideWallInnerWinWallArg = new DivideArg(new Size(.7f, false), win_wall);
		LinkedList<DivideArg> divideWallInnerArgs = new LinkedList<DivideArg>();
		divideWallInnerArgs.add(divideWallInnerCobblestoneArg);
		divideWallInnerArgs.add(divideWallInnerDoorArg);
		divideWallInnerArgs.add(divideWallInnerWinWallArg);
		DivideRule divideWallInner = new DivideRule(divideWallInnerArgs, DivideRule.Direction.X);
		
		DivideArg divideWinWallBottomArg = new DivideArg(new Size(1f, true), setCobblestone.clone());
		DivideArg divideWinWallMiddleArg = new DivideArg(new Size(1f, true), setGlass.clone());
		DivideArg divideWinWallTopArg = new DivideArg(new Size(1f, true), setCobblestone.clone());
		LinkedList<DivideArg> divideWinWallArgs = new LinkedList<DivideArg>();
		divideWinWallArgs.add(divideWinWallBottomArg);
		divideWinWallArgs.add(divideWinWallMiddleArg);
		divideWinWallArgs.add(divideWinWallTopArg);
		DivideRule divideWinWall = new DivideRule(divideWinWallArgs, DivideRule.Direction.Y);
		
		DivideArg doorAirArg = new DivideArg(new Size(2f, true), setAir);
		DivideArg doorTopArg = new DivideArg(new Size(1f, true), setCobblestone);
		LinkedList<DivideArg> divideDoorArgs = new LinkedList<DivideArg>();
		divideDoorArgs.add(doorAirArg);
		divideDoorArgs.add(doorTopArg);
		DivideRule divideDoor = new DivideRule(divideDoorArgs, DivideRule.Direction.Y);
		
		DivideArg divideMiddlePartWallsLeft = new DivideArg(new Size(1f, true), setOaktrunk.clone());
		DivideArg divideMiddlePartWallsCenter = new DivideArg(new Size(1f, false), setStone.clone());
		DivideArg divideMiddlePartWallsRight = new DivideArg(new Size(1f, true), setOaktrunk.clone());
		LinkedList<DivideArg> divideMiddlePartWallsArgs = new LinkedList<DivideArg>();
		divideMiddlePartWallsArgs.add(divideMiddlePartWallsLeft);
		divideMiddlePartWallsArgs.add(divideMiddlePartWallsCenter);
		divideMiddlePartWallsArgs.add(divideMiddlePartWallsRight);
		DivideRule divideMiddlePartWalls = new DivideRule(divideMiddlePartWallsArgs, DivideRule.Direction.X);
		//================================================
		SplitArg splitWallArg = new SplitArg(SplitArg.SplitType.WALLS, wall);
		SplitRule splitGroundFloor = new SplitRule(Arrays.asList(splitWallArg));
		
		SplitArg splitBorderArg = new SplitArg(SplitArg.SplitType.WALLS, setOaktrunk);
		SplitRule splitBorder = new SplitRule(Arrays.asList(splitBorderArg));
		
		//SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, divideMiddlePartWalls);
		//SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, setCobblestone.clone());
		SplitArg splitMiddlePartArg = new SplitArg(SplitArg.SplitType.WALLS, middle_part_walls);
		SplitRule splitMiddlePart = new SplitRule(Arrays.asList(splitMiddlePartArg));
		//================================================
		Map<String, List<Shape>> rules = new HashMap<String, List<Shape>>();
		rules.put(house.getLabel(), Arrays.<Shape>asList(divideHouse));
		rules.put(ground_floor.getLabel(), Arrays.<Shape>asList(splitGroundFloor));
		rules.put(wall.getLabel(), Arrays.<Shape>asList(divideWall));
		rules.put(wall_inner.getLabel(), Arrays.<Shape>asList(divideWallInner));
		rules.put(win_wall.getLabel(), Arrays.<Shape>asList(divideWinWall));
		rules.put(door.getLabel(), Arrays.<Shape>asList(divideDoor));
		rules.put(border.getLabel(), Arrays.<Shape>asList(splitBorder));
		rules.put(middle_part.getLabel(), Arrays.<Shape>asList(splitMiddlePart));
		rules.put(middle_part_walls.getLabel(), Arrays.<Shape>asList(divideMiddlePartWalls));
		
		ProductionSystem system = new ProductionSystem(rules, house);
		Grammar grammar = new Grammar(system);
		
		return new BuildingGenerator(grammar);
	 }
	 
	 private static List<String> getBlocks(int index){
		 List<String> retval = new ArrayList<String>();
		 switch(index){
		 	case 1:{
		 		retval.add("chocolatetop");
		 		retval.add("chocolateblock");
		 		retval.add("chocolatebrownietop");
		 		retval.add("glass");
		 		retval.add("RedVelvetCakeTop");
		 		retval.add("air");
		 		break;
		 	}
		 	case 2:{
		 		retval.add("Strawberry3StepsCake2Block");
		 		retval.add("Strawberry3StepsCake1Block");
		 		retval.add("Strawberry3StepsCake2Block");
		 		retval.add("glass");
		 		retval.add("Strawberry3StepsCake3Block");
		 		retval.add("air");
		 		break;
		 	}
		 	case 3:{
		 		retval.add("kiwicaketop");
		 		retval.add("kiwicakeblock");
		 		retval.add("kiwicaketop");
		 		retval.add("glass");
		 		retval.add("chocolateblock");
		 		retval.add("air");
		 		break;
		 	}
		 	case 4:{
		 		retval.add("BlueberryChristmasCakeTop");
		 		retval.add("BlueberryChristmasBlock");
		 		retval.add("BlueberryChristmasCakeTop");
		 		retval.add("glass");
		 		retval.add("chocolateblock");
		 		retval.add("air");
		 		break;
		 	}
		 	case 5:{
		 		retval.add("CitrusCakeTop");
		 		retval.add("CitrusCakeBlock");
		 		retval.add("CitrusCakeTop");
		 		retval.add("glass");
		 		retval.add("CornyChokoTop");
		 		retval.add("air");
		 		break;
		 	}
		 	case 6:{
		 		retval.add("OrangeCakeTop");
		 		retval.add("OrangeCakeBlock");
		 		retval.add("OrangeCakeTop");
		 		retval.add("glass");
		 		retval.add("CornyChokoBlock");
		 		retval.add("air");
		 		break;
		 	}
		 	case 7:{
		 		retval.add("RasberryCakeTop");
		 		retval.add("RasberryCakeBlock");
		 		retval.add("RasberryCakeTop");
		 		retval.add("glass");
		 		retval.add("RasberrySoftCakeTop");
		 		retval.add("air");
		 		break;
		 	}
		 	case 8:{
		 		retval.add("RedVelvetCakeTop");
		 		retval.add("RedVelvetCakeBlock");
		 		retval.add("RedVelvetCakeTop");
		 		retval.add("glass");
		 		retval.add("RasberrySoftCakeBlock");
		 		retval.add("air");
		 		break;
		 	}
		 	case 9:{
		 		retval.add("StrawberryJapanCakeTop");
		 		retval.add("StrawberryJapanCakeBlock");
		 		retval.add("StrawberryJapanCakeTop");
		 		retval.add("glass");
		 		retval.add("RedVelvetCakeTop");
		 		retval.add("air");
		 		break;
		 	}
		 	default : {
		 		retval.add("chocolatetop");
		 		retval.add("chocolateblock");
		 		retval.add("chocolatebrownietop");
		 		retval.add("glass");
		 		retval.add("chocolateblock");
		 		retval.add("air");
		 		break;
		 	}
		 }
		 return retval;
	 }
}
