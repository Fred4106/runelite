import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.MenuOpcode
import net.runelite.api.events.GameTick
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.widgets.WidgetID
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.cs2.InterfaceChoice
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

import static net.runelite.api.ItemID.RING_OF_DUELING1
import static net.runelite.api.ItemID.RING_OF_DUELING2
import static net.runelite.api.ItemID.RING_OF_DUELING3
import static net.runelite.api.ItemID.RING_OF_DUELING4
import static net.runelite.api.ItemID.RING_OF_DUELING5
import static net.runelite.api.ItemID.RING_OF_DUELING6
import static net.runelite.api.ItemID.RING_OF_DUELING7
import static net.runelite.api.ItemID.RING_OF_DUELING8
import static net.runelite.api.ItemID.GAMES_NECKLACE1
import static net.runelite.api.ItemID.GAMES_NECKLACE2
import static net.runelite.api.ItemID.GAMES_NECKLACE3
import static net.runelite.api.ItemID.GAMES_NECKLACE4
import static net.runelite.api.ItemID.GAMES_NECKLACE5
import static net.runelite.api.ItemID.GAMES_NECKLACE6
import static net.runelite.api.ItemID.GAMES_NECKLACE7
import static net.runelite.api.ItemID.GAMES_NECKLACE8

@CompileStatic
@InheritConstructors
class Demo extends ScriptedPlugin
{

	int[] RingOfDuelingID = [RING_OF_DUELING1,RING_OF_DUELING2,RING_OF_DUELING3,RING_OF_DUELING4,RING_OF_DUELING5,RING_OF_DUELING6,RING_OF_DUELING7,RING_OF_DUELING8];
	int[] GamesNecklaceID = [GAMES_NECKLACE1,GAMES_NECKLACE2,GAMES_NECKLACE3,GAMES_NECKLACE4,GAMES_NECKLACE5,GAMES_NECKLACE6,GAMES_NECKLACE7,GAMES_NECKLACE8];
	private final String[] RingOfDuelingOptions = new String[] { "Duel", "Castle", "Clan"};
	private final String[] GamesNecklaceOptions = new String[] { "Burthorpe", "Barbarian Outpost", "Tears of Guthix", "Wintertodt Camp"};
	String targetWord = "";
	private final String[] skillingTargets = new String[] { "Water battlestaff", "Maple longbow", "Water orb"};
	void onInterfaceChoice(InterfaceChoice event)
	{
		log(LogLevel.DEBUG, event.toString())
		if(event.getOptionCount() == 1)
		{
			event.requestOption(1);
		}
		else if(targetWord.length() > 0)
		{
			for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
			{
				String op = event.getOption(i).toLowerCase();
				if (skillingTargets.any(it -> it.equalsIgnoreCase(op)))
				{
					event.requestOption(i);
				}
				else if(op.containsIgnoreCase(targetWord))
				{
					event.requestOption(i);
					targetWord = "";
				}
			}
		}
		else
		{
			for(int i = 1; i <= event.getOptionCount() && event.free(); i++)
			{
				String op = event.getOption(i).toLowerCase();
				if(op.containsIgnoreCase("yes"))
				{
					event.requestOption(i);
				}
			}
		}
	}

	void onMenuEntryAdded(MenuEntryAdded e)
	{
		if (WidgetID.INVENTORY_GROUP_ID == WidgetInfo.TO_GROUP(e.getParam1()) && e.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId() && e.getOption().contains("Rub")) //check item is in inventory
		{
			if (RingOfDuelingID.any {int it -> e.getIdentifier() == it} )
			{
				for(int a = 0; a < RingOfDuelingOptions.length; a++)
				{
					_client.insertMenuItem(RingOfDuelingOptions[a], e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
				}
			}
			else if (GamesNecklaceID.any {int it -> e.getIdentifier() == it} )
			{
				for(int a = 0; a < GamesNecklaceOptions.length; a++)
				{
					_client.insertMenuItem(GamesNecklaceOptions[a], e.getTarget(), e.getOpcode(), e.getIdentifier(), e.getParam0(), e.getParam1(), false);
				}
			}
		}
	}

	void onMenuOptionClicked(MenuOptionClicked e)
	{
		if (WidgetID.INVENTORY_GROUP_ID == WidgetInfo.TO_GROUP(e.getParam1()) && e.getOpcode() == MenuOpcode.ITEM_FOURTH_OPTION.getId() && (RingOfDuelingID.any {int it -> e.getIdentifier() == it} || GamesNecklaceID.any {int it -> e.getIdentifier() == it}))
		{

			if(!e.getOption().equalsIgnoreCase("Rub"))
			{
				targetWord = e.getOption();
			}
		}
		log(LogLevel.DEBUG, e.toString());
	}



	void startup()
	{
		_eventBus.subscribe(InterfaceChoice.class, this, this.&onInterfaceChoice as Consumer<InterfaceChoice>)

		_eventBus.subscribe(MenuOptionClicked.class, this, this::onMenuOptionClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this::onMenuEntryAdded as Consumer<MenuEntryAdded>);
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}

	static int TO_GROUP(int id)
	{
		return id >>> 16;
	}

	/**
	 * Utility method that converts an ID returned by child back
	 * to its child ID.
	 *
	 * @param id passed group-child ID
	 * @return the child ID
	 */
	static int TO_CHILD(int id)
	{
		return id & 0xFFFF;
	}

	/**
	 * Packs the group and child IDs into a single integer.
	 *
	 * @param groupId the group ID
	 * @param childId the child ID
	 * @return the packed ID
	 */
	static int PACK(int groupId, int childId)
	{
		return groupId << 16 | childId;
	}
}
