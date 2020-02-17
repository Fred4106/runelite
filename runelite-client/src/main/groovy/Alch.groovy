import groovy.transform.CompileStatic;
import groovy.transform.InheritConstructors
import io.reactivex.functions.Consumer
import net.runelite.api.ItemID
import net.runelite.api.MenuOpcode
import net.runelite.api.events.MenuEntryAdded
import net.runelite.api.events.MenuOptionClicked
import net.runelite.api.events.ScriptCallbackEvent;
import net.runelite.api.vars.InterfaceTab
import net.runelite.api.widgets.WidgetInfo
import net.runelite.client.plugins.groovy.debugger.DebuggerWindow.LogLevel
import net.runelite.client.plugins.groovy.script.ScriptedPlugin

@InheritConstructors
class Alch extends ScriptedPlugin {

	int[] items_to_alch = [
		ItemID.WATER_BATTLESTAFF, ItemID.EARTH_BATTLESTAFF, ItemID.AIR_BATTLESTAFF, ItemID.FIRE_BATTLESTAFF,
		ItemID.MAPLE_LONGBOW, ItemID.YEW_SHORTBOW, ItemID.YEW_LONGBOW
	];
//	Map<Integer, List<Integer>> items_to_enchant = [
//		0: ,
//		1: [ItemID.EMERALD_RING, ItemID.EMERALD_AMULET, ItemID.EMERALD_NECKLACE, ItemID.EMERALD_BRACELET],
//		2: [ItemID.RUBY_RING, ItemID.RUBY_AMULET, ItemID.RUBY_NECKLACE, ItemID.RUBY_BRACELET],
//		3: [ItemID.DIAMOND_RING, ItemID.DIAMOND_AMULET, ItemID.DIAMOND_NECKLACE, ItemID.DIAMOND_BRACELET],
//	];

	List<Tuple3<WidgetInfo, String, List<Integer>>> items_to_enchant = [
		new Tuple3<>(WidgetInfo.SPELL_LVL_1_ENCHANT, "Lvl-1 Enchant", [ItemID.SAPPHIRE_RING, ItemID.SAPPHIRE_AMULET, ItemID.SAPPHIRE_NECKLACE, ItemID.SAPPHIRE_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_2_ENCHANT, "Lvl-2 Enchant", [ItemID.EMERALD_RING, ItemID.EMERALD_AMULET, ItemID.EMERALD_NECKLACE, ItemID.EMERALD_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_3_ENCHANT, "Lvl-3 Enchant", [ItemID.RUBY_RING, ItemID.RUBY_AMULET, ItemID.RUBY_NECKLACE, ItemID.RUBY_BRACELET]),
		new Tuple3<>(WidgetInfo.SPELL_LVL_4_ENCHANT, "Lvl-4 Enchant", [ItemID.DIAMOND_RING, ItemID.DIAMOND_AMULET, ItemID.DIAMOND_NECKLACE, ItemID.DIAMOND_BRACELET])
	];

	boolean latch = false;

	void onMenuClicked(MenuOptionClicked e) {
		if (!e.getOption().equalsIgnoreCase("<col=0000ff>Cast")) {
			return;
		}
		e.setOption("Cast");
		_client.setSelectedSpellChildIndex(-1);
		log(LogLevel.DEBUG, "fuck me");
		if(e.getTarget().contains("High Level Alchemy")) {
			_client.setSelectedSpellName("<col=00ff00>High Level Alchemy</col>");
			_client.setSelectedSpellWidget(WidgetInfo.SPELL_HIGH_LEVEL_ALCHEMY.getId());
			latch = true;
		} else if(e.getTarget().contains("Enchant")) {
			Tuple3<WidgetInfo, String, List<Integer>> selected = items_to_enchant.stream().filter(f -> e.getTarget().contains(f.getV2())).findFirst().orElse(null);
			if(selected != null)
			{
				_client.setSelectedSpellName("<col=00ff00>${selected.getV2()}</col>");
				_client.setSelectedSpellWidget(selected.getV1().getId());
				latch = true;
			}
		}
		log(LogLevel.DEBUG, "menu hijacked -> ${e.getOption()}");
	}

	void onMenuAdded(MenuEntryAdded e) {
		if (e.getOpcode() != MenuOpcode.ITEM_USE.getId()) {
			return;
		}
		if (items_to_alch.contains(e.getIdentifier()) || items_to_alch.contains(e.getIdentifier()-1)) {
			_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>High Level Alchemy</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
		}
		for (int i = 0; i < items_to_enchant.size(); i++)
		{
			if (items_to_enchant[i].getV3().contains(e.getIdentifier()))
			{
				_client.insertMenuItem("<col=0000ff>Cast", "<col=00ff00>${items_to_enchant[i].getV2()}</col><col=ffffff> -> <col=ff9040>${_client.getItemDefinition(e.getIdentifier()).getName()}", MenuOpcode.ITEM_USE_ON_WIDGET.getId(), e.getIdentifier(), e.param0, e.param1, false);
			}
		}
	}

	void onScriptCallbackEvent(ScriptCallbackEvent e)
	{
		if (e.getEventName().equalsIgnoreCase("OnSwitchTopLevel2"))
		{
			int[] intStack = _client.getIntStack();
			log(LogLevel.ERROR, "Latch: ${latch}");
			if(latch)
			{
				log(LogLevel.DEBUG, "intStack = ${intStack}");
				intStack[0] = InterfaceTab.INVENTORY.id;
				log(LogLevel.WARN, "intStack = ${intStack}");
				latch = false;
			}
		}
	}

	void startup() {
		_eventBus.subscribe(MenuOptionClicked.class, this, this.&onMenuClicked as Consumer<MenuOptionClicked>);
		_eventBus.subscribe(MenuEntryAdded.class, this, this.&onMenuAdded as Consumer<MenuEntryAdded>);
		_eventBus.subscribe(ScriptCallbackEvent.class, this, this.&onScriptCallbackEvent as Consumer<ScriptCallbackEvent>);
		log(LogLevel.DEBUG, "Starting Up");
	}

	void shutdown() {
		_eventBus.unregister(this);
		log(LogLevel.DEBUG,"Shutting Down");
	}
}