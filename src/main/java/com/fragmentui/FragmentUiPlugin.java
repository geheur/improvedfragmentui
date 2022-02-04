package com.fragmentui;

import com.google.inject.Provides;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.Point;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.MenuEntryAdded;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.JavaScriptCallback;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SpriteManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.ui.overlay.OverlayPosition;

@Slf4j
@PluginDescriptor(
	name = "Improved Fragment UI"
)
public class FragmentUiPlugin extends Plugin
{
	public static final String GROUP_NAME = "improvedfragmentui";

	@Inject
	private Client client;

	@Inject
	private FragmentUiConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SpriteManager spriteManager;

	private String lastInput = "";

	@Subscribe
	public void onClientTick(ClientTick e) {
		Widget equippedFragmentsWidget = client.getWidget(735, 35);
		if (equippedFragmentsWidget != null && !equippedFragmentsWidget.isHidden()) {
			if (config.changeFragmentIcons())
			{
				changeFragmentIcons(equippedFragmentsWidget);
			}
			if (config.swapViewEquip()) swapViewEquip();
			if (config.filterFragments()) filterFragments();
			for (int i = 0; i < equippedFragmentsWidget.getDynamicChildren().length; i += 6)
			{
				Widget fragmentSymbol = equippedFragmentsWidget.getDynamicChildren()[i + 1];
				fragmentSymbol.setDragDeadTime(10);
			}
		}
	}

	private void filterFragments()
	{
		String input = client.getVarcStrValue(335);
		if (!input.equals(lastInput))
		{
			lastInput = input;
			updateFilter(lastInput);
			client.runScript(5751, 48168977, 48168978);
		}
	}

	private void swapViewEquip()
	{
		MenuEntry[] entries = client.getMenuEntries();
		if (entries.length == 3 && entries[2].getOption().equals("View")) {
			MenuEntry menuEntry = entries[2];
			entries[2] = entries[1];
			entries[1] = menuEntry;
		}
		client.setMenuEntries(entries);
	}

	private void changeFragmentIcons(Widget equippedFragmentsWidget)
	{
		for (int i = 0; i < equippedFragmentsWidget.getDynamicChildren().length; i += 6)
		{
			Widget fragmentBackground = equippedFragmentsWidget.getDynamicChildren()[i + 1];
			Widget fragmentSymbol = equippedFragmentsWidget.getDynamicChildren()[i + 2];
			Integer replacementItemId = icons.get(fragmentSymbol.getSpriteId());
			if (replacementItemId != null) {
				fragmentBackground.setSpriteId(-1);
				fragmentBackground.setItemQuantityMode(0);
				if (replacementItemId > 0) {
					fragmentBackground.setItemId(replacementItemId);
				} else {
					fragmentBackground.setSpriteId(replacementItemId * -1);
					fragmentBackground.setSpriteTiling(false);
				}
				fragmentSymbol.setSpriteId(-1);
				equippedFragmentsWidget.getDynamicChildren()[i + 0].setSpriteId(-1);
				equippedFragmentsWidget.getDynamicChildren()[i + 3].setSpriteId(-1);
			}
		}

		Widget widget = client.getWidget(735, 17);
		if (widget == null || widget.isHidden()) return;

		for (int i = 0; i < widget.getDynamicChildren().length; i += 9)
		{
			int symbolId = widget.getDynamicChildren()[i + 3].getSpriteId();
			int finalI = i;
			widget.getDynamicChildren()[i + 2].setOnDragCompleteListener((JavaScriptCallback) e -> {
				if (!lastInput.equals("")) return;
				Point mouseCanvasPosition = client.getMouseCanvasPosition();
				if (!widget.getBounds().contains(mouseCanvasPosition.getX(), mouseCanvasPosition.getY())) return;
				int scrollY = widget.getScrollY();
				int fragmentIdDraggedOn = (mouseCanvasPosition.getY() - widget.getCanvasLocation().getY() + scrollY) / 42;
				int fragmentIdDragged = customSort.get(finalI / 9);
				int[] reverseCustomSort = new int[53];
				for (int i1 = 0; i1 < customSort.size(); i1++)
				{
					reverseCustomSort[customSort.get(i1)] = i1;
				}
				int swap = reverseCustomSort[fragmentIdDragged];
				if (fragmentIdDragged > fragmentIdDraggedOn) {
					for (int i2 = fragmentIdDragged; i2 > fragmentIdDraggedOn; i2--)
					{
						reverseCustomSort[i2] = reverseCustomSort[i2 - 1];
					}
				} else {
					for (int i2 = fragmentIdDragged; i2 < fragmentIdDraggedOn; i2++)
					{
						reverseCustomSort[i2] = reverseCustomSort[i2 + 1];
					}
				}
				reverseCustomSort[fragmentIdDraggedOn] = swap;
				for (int i1 = 0; i1 < reverseCustomSort.length; i1++)
				{
					customSort.set(reverseCustomSort[i1], i1);
				}
				updateFilter(lastInput);
				configManager.setConfiguration(GROUP_NAME, "customsort", String.join(",", customSort.stream().map(s -> "" + s).collect(Collectors.toList())));
				client.runScript(5751, 48168977, 48168978);
			});
			Integer replacementItemId = icons.get(symbolId);
			if (replacementItemId != null) {
				widget.getDynamicChildren()[i + 1].setSpriteId(-1);
				widget.getDynamicChildren()[i + 4].setSpriteId(-1);
				widget.getDynamicChildren()[i + 5].setSpriteId(-1);
				widget.getDynamicChildren()[i + 3].setSpriteId(-1);
				widget.getDynamicChildren()[i + 2].setSpriteId(-1);
				if (replacementItemId > 0) {
					widget.getDynamicChildren()[i + 2].setItemId(replacementItemId);
				} else {
					widget.getDynamicChildren()[i + 2].setSpriteId(replacementItemId * -1);
					widget.getDynamicChildren()[i + 2].setSpriteTiling(false);
				}
				widget.getDynamicChildren()[i + 2].setItemQuantityMode(0);
			}
		}
	}

	List<Integer> customSort = null;

	public int getOrder(int fragmentId) {
		return filteredSortedList.get(fragmentId - 1);
	}

	@Subscribe
	public void onScriptPreFired(ScriptPreFired e) {
		if (e.getScriptId() == 5752 && config.filterFragments()) {
			int struct = client.getIntStack()[client.getIntStackSize() - 2];

			StructComposition structComposition = client.getStructComposition(struct);

			int fragmentId = structComposition.getIntValue(1455);
			int order = getOrder(fragmentId);
			client.getIntStack()[client.getIntStackSize() - 1] = order == -1 ? 1 : 0;
			if (order != -1) client.getIntStack()[client.getIntStackSize() - 3] = order;
		}
	}

	List<Integer> filteredSortedList = new ArrayList<>();

	private void updateFilter(String input)
	{
		filteredSortedList.clear();
		String relics = configManager.getConfiguration(GROUP_NAME, "tag_" + input);
		outer:
		for (int i = 0; i < customSort.size(); i++)
		{
			String[] words = input.split(" ");

			StructComposition structComposition = client.getStructComposition(fragmentIdToStructId.get(i));
			String name = structComposition.getStringValue(1448);
			SetEffect setEffect1 = SetEffect.values()[structComposition.getIntValue(1459) - 1];
			SetEffect setEffect2 = SetEffect.values()[structComposition.getIntValue(1460) - 1];
			String setEffect1Name = setEffect1.name;
			String setEffect2Name = setEffect2.name;

			boolean include = false;

			if (relics != null) {
				String[] relicIds = relics.split(" ");
				for (int j = 0; j < relicIds.length; j++) {
					if (Integer.parseInt(relicIds[j]) == i + 1) {
						include = true;
						break;
					}
				}
			} else {
				for (String word : words) {
					if (name.toLowerCase().contains(word) || setEffect1Name.toLowerCase().contains(word) || setEffect2Name.toLowerCase().contains(word)) {
						include = true;
						break;
					}
				}
			}

			filteredSortedList.add(include ? customSort.get(i) : -1);
		}
		List<Integer> ascendingFilteredSortedList = IntStream.range(0, 53).mapToObj(i -> i).collect(Collectors.toList());
		Collections.sort(ascendingFilteredSortedList, (i1, i2) -> {
			Integer integer1 = filteredSortedList.get(i1);
			Integer integer2 = filteredSortedList.get(i2);
			return integer1.compareTo(integer2);
		});
		int order = 0;
		for (int i = 0; i < ascendingFilteredSortedList.size(); i++)
		{
			int index = ascendingFilteredSortedList.get(i);
			int unfilteredIndex = filteredSortedList.get(index);
			if (unfilteredIndex == -1) continue;
			filteredSortedList.set(index, order);
			order++;
		}
	}

	@Subscribe
	public void onCommandExecuted(CommandExecuted e) {
		if (e.getCommand().equals("fragui")) {
			String[] arguments = e.getArguments();
			if (arguments.length == 2 && arguments[0].equalsIgnoreCase("sort") && arguments[1].equalsIgnoreCase("alpha")) {
				alphaSort();
			}
			if (arguments.length == 2 && arguments[0].equalsIgnoreCase("sort") && arguments[1].equalsIgnoreCase("reset")) {
				customSort = new ArrayList<>(IntStream.range(0, 53).mapToObj(i -> i).collect(Collectors.toList()));
				updateFilter(lastInput);
				configManager.setConfiguration(GROUP_NAME, "customsort", String.join(",", customSort.stream().map(s -> "" + s).collect(Collectors.toList())));
			}
			if (arguments.length == 2 && (arguments[0].equalsIgnoreCase("set") || arguments[0].equalsIgnoreCase("preset") || arguments[0].equalsIgnoreCase("tag"))) {
				tagFragments(arguments[1]);
			}
			if (arguments.length == 1 && arguments[0].equalsIgnoreCase("list")) {
				listSets();
			}
			if (arguments.length == 2 && arguments[0].equalsIgnoreCase("delete")) {
				deleteSet(arguments[1]);
			}
		}
	}

	private void listSets()
	{
		String message = "Fragment sets: ";
		message += String.join(", ", configManager.getConfigurationKeys(GROUP_NAME + ".tag_").stream().map(s -> s.substring((GROUP_NAME + ".tag_").length())).collect(Collectors.toList()));
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", message, "bla");
	}

	private void tagFragments(String tagName)
	{
		tagName = tagName.trim();
		List<String> relics = new ArrayList<>();
		for (int i = 0; i < 7; i++)
		{
			int slotValue = client.getVarbitValue(13395 + i + (i >= 5 ? 1 : 0));
			relics.add("" + slotValue);
		}
		boolean modified = configManager.getConfiguration(GROUP_NAME, "tag_" + tagName) != null;
		configManager.setConfiguration(GROUP_NAME, "tag_" + tagName, String.join(" ", relics));
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", (modified ? "Modified" : "Added") + " set \"" + tagName + "\"", "bla");
	}

	private void deleteSet(String tagName)
	{
		boolean exists = configManager.getConfiguration(GROUP_NAME, "tag_" + tagName) != null;
		if (!exists) {
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", "Preset \"" + tagName + "\" does not exist.", "bla");
			return;
		}
		configManager.unsetConfiguration(GROUP_NAME, "tag_" + tagName);
		client.addChatMessage(ChatMessageType.GAMEMESSAGE, "bla", "Deleted \"" + tagName + "\"", "bla");
	}

	private void alphaSort()
	{
		List<Integer> fragmentOrder = new ArrayList<>(IntStream.range(0, 53).mapToObj(i -> i).collect(Collectors.toList()));
		List<String> fragmentNames = new ArrayList<>();
		for (Integer fragmentId : fragmentIdToStructId)
		{
			StructComposition structComposition = client.getStructComposition(fragmentId);
			String name = structComposition.getStringValue(1448);
			fragmentNames.add(name);
		}
		fragmentOrder.sort((i1, i2) -> {
			String name1 = fragmentNames.get(i1);
			String name2 = fragmentNames.get(i2);
			return name1.compareTo(name2);
		});
		customSort = new ArrayList<>();
		for (int i = 0; i < 53; i++)
		{
			customSort.add(-1);
		}
		int i = 0;
		for (Integer integer : fragmentOrder)
		{
			customSort.set(integer, i);
			i++;
		}
		updateFilter(lastInput);
		configManager.setConfiguration(GROUP_NAME, "customsort", String.join(",", customSort.stream().map(s -> "" + s).collect(Collectors.toList())));
	}

	private FragmentUiOverlay overlay = new FragmentUiOverlay();

	@Inject
	private ClientThread clientThread;

	@Override
	protected void startUp() throws Exception
	{
		String sortConfig = configManager.getConfiguration(GROUP_NAME, "customsort");
		if (sortConfig == null) customSort = new ArrayList<>(IntStream.range(0, 53).mapToObj(i -> i).collect(Collectors.toList()));
		else {
			customSort = new ArrayList<>(Arrays.asList(sortConfig.split(",")).stream().map(i -> Integer.parseInt(i)).collect(Collectors.toList()));
			clientThread.invokeLater(() -> updateFilter(lastInput));
		}
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(overlay);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
	}

	@Provides
	FragmentUiConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(FragmentUiConfig.class);
	}

	public enum SetEffect {
		ABSOLUTE_UNIT("Absolute Unit", 22326),
		THE_ALCHEMIST("The Alchemist", 2428),
		CHAIN_MAGIC("Chain Magic", 6889),
		THE_CRAFTSMAN("The Craftsman", 1755),
		DOUBLE_TAP("Double Tap", 11785),
		DRAKANS_TOUCH("Drakan's Touch", 565),
		ENDLESS_KNOWLEDGE("Endless Knowledge", 26551),
		FAST_METABOLISM("Fast Metabolism", -122),
		GREEDY_GATHERER("Greedy Gatherer", 1004),
		KNIFES_EDGE("Knife's Edge", 4718),
		LAST_RECALL("Last Recall", 25104),
		PERSONAL_BANKER("Personal Banker", -1453),
		TRAILBLAZER("Trailblazer", 26549),
		TWIN_STRIKES("Twin Strikes", 4587),
		UNCHAINED_TALENT("Unchained Talent", 7479),
		;

		SetEffect(String name, int iconId) {
			this.name = name;
			this.iconId = iconId;
		}

		public String name;
		public int iconId;
	}

	Map<Integer, Integer> icons = new HashMap<>();
	{
	icons.put(3920, -145); // Unholy Warrior
	icons.put(3919, 12006); // Tactical Duelist
	icons.put(3903, -508); // Unholy Ranger
	icons.put(3902, 11216); // Bottomless Quiver
	icons.put(3881, -509); // Unholy Wizard
	icons.put(3879, 556); // Arcane Conduit
	icons.put(3880, 25818); // Thrall Damage
	icons.put(3906, 544); // Livin' On A Prayer
	icons.put(3905, 1718); // Divine Restoration
	icons.put(3907, 536); // Praying Respects
	icons.put(3923, 1215); // Larger Recharger
	icons.put(3924, 13652); // Special Discount
	icons.put(3935, 2446); // Venomaster
	icons.put(3901, -216); // Slay All Day
	icons.put(3900, 1451); // Superior Tracking
	icons.put(3899, 4155); // Slay 'n' Pay
	icons.put(3921, 11832); // Bandosian Might
	icons.put(3904, 11828); // Armadylean Decree
	icons.put(3882, 1035); // Zamorakian Sight
	icons.put(3922, 13526); // Saradominist Defence
	icons.put(3912, 1949); // Chef's Catch
	icons.put(3913, 2366); // Catch Of The Day
	icons.put(3917, 21143); // Smooth Criminal
	icons.put(3918, 5554); // Deeper Pockets
	icons.put(3915, 590); // Slash & Burn
	icons.put(3916, 5075); // Homewrecker
	icons.put(3933, 24365); // Hot on the Trail
	icons.put(3889, 8778); // Plank Stretcher
	icons.put(3909, 440); // Rock Solid
	icons.put(3908, -36); // Molten Miner
	icons.put(3885, 2363); // Smithing Double
	icons.put(3886, 1777); // Rumple-Bow-String
	icons.put(3887, 1753); // Dragon On a Bit
	icons.put(3895, 1679); // Imcando's Apprentice
	icons.put(3883, 11190); // Enchanted Jeweler
	icons.put(3884, -41); // Alchemaniac
	icons.put(3930, 62); // Profletchional
	icons.put(3931, 9226); // Pro Tips
	icons.put(3914, 9976); // Chinchonkers
	icons.put(3898, 2301); // Dine & Dash
	icons.put(3910, 7409); // Certified Farmer
	icons.put(3911, 5255); // Seedy Business
	icons.put(3928, 99); // Mixologist
	icons.put(3929, 203); // Just Druid!
	icons.put(3890, 11849); // Golden Brick Road
	icons.put(3891, 24716); // Grave Robber
	icons.put(3772, 7936); // Rooty Tooty
	icons.put(3892, 558); // Rune Escape
	icons.put(3896, 24363); // Clued In
	icons.put(3897, 13649); // Message In A Bottle
	icons.put(3893, 11664); // Barbarian Pest Wars
	icons.put(3894, 13646); // Rogues' Chompy Farm
		// This one shares an icon with catch of the day.
//	icons.put(3913, 6887); // Mother's Magic Fossils
	}

	List<Integer> fragmentIdToStructId = Arrays.asList(new Integer[]{4038, 4041, 4039, 4042, 4040, 4043, 4054, 4037, 4053, 4061, 4047, 4048, 4089, 4045, 4046, 4044, 4083, 4084, 4085, 4086, 4049, 4058, 4051, 4052, 4050, 4055, 4056, 4057, 4059, 4060, 4073, 4077, 4078, 4072, 4067, 4068, 4064, 4065, 4074, 4066, 4079, 4080, 4087, 4088, 4062, 4063, 4075, 4076, 4081, 4082, 4069, 4070, 4071});

	private class FragmentUiOverlay extends Overlay
	{
		{
			setPosition(OverlayPosition.DYNAMIC);
			setLayer(OverlayLayer.ALWAYS_ON_TOP);
		}

		@Override
		public Dimension render(Graphics2D graphics)
		{
			if (!config.showSetEffectIcons()) return null;

			Widget equippedFragmentsWidget = client.getWidget(735, 35);
			if (equippedFragmentsWidget == null || equippedFragmentsWidget.isHidden()) return null;
//				log.info("length: " + equippedFragmentsWidget.getDynamicChildren().length);
			for (int i = 0; i < equippedFragmentsWidget.getDynamicChildren().length / 6; i++)
			{
				// <3 hydrox.
				// 13400, where slot 6 would be, isn't the contents of slot 6!. Jank!
				int slotValue = client.getVarbitValue(13395 + i + (i >= 5 ? 1 : 0));
				if (slotValue == 0) continue; // no relic in this slot.
				Integer structId = fragmentIdToStructId.get(slotValue - 1);
				if (structId == null) continue; // shouldn't happen.
				StructComposition structComposition = client.getStructComposition(structId);
				SetEffect setEffect1 = SetEffect.values()[structComposition.getIntValue(1459) - 1];
				SetEffect setEffect2 = SetEffect.values()[structComposition.getIntValue(1460) - 1];

				Widget fragmentBackground = equippedFragmentsWidget.getDynamicChildren()[i * 6 + 1];

				int iconSize = 21;
				int x1 = fragmentBackground.getCanvasLocation().getX() - 3;
				int x2 = fragmentBackground.getCanvasLocation().getX() - 3 + 21;
				int y = fragmentBackground.getCanvasLocation().getY() - 22;
				if (setEffect1.iconId > 0) {
					BufferedImage image = itemManager.getImage(setEffect1.iconId);
					graphics.drawImage(image, x1, y, iconSize + 2, iconSize + 2, null, null);
				} else {
					BufferedImage image = spriteManager.getSprite(setEffect1.iconId * -1, 0);
					graphics.drawImage(image, x1, y, iconSize, iconSize, null, null);
				}

				if (setEffect2.iconId > 0) {
					BufferedImage image = itemManager.getImage(setEffect2.iconId);
					graphics.drawImage(image, x2, y, iconSize + 2, iconSize + 2, null, null);
				} else {
					BufferedImage image = spriteManager.getSprite(setEffect2.iconId * -1, 0);
					graphics.drawImage(image, x2, y, iconSize, iconSize, null, null);
				}

			}
			Widget widget = client.getWidget(735, 17);
			if (widget == null || widget.isHidden()) return null;

			for (int i = 0; i < widget.getDynamicChildren().length / 9; i++)
			{
				int fragmentId = i + 1;
				Integer structId = fragmentIdToStructId.get(fragmentId - 1);
				if (structId == null) continue; // shouldn't happen.
				StructComposition structComposition = client.getStructComposition(structId);
				SetEffect setEffect1 = SetEffect.values()[structComposition.getIntValue(1459) - 1];
				SetEffect setEffect2 = SetEffect.values()[structComposition.getIntValue(1460) - 1];

				Rectangle bounds = client.getWidget(735, 16).getBounds();
				graphics.setClip((int) bounds.getX(), (int) bounds.getY() - 0, (int) bounds.getWidth(), (int) bounds.getHeight());
				// graphics.drawRect((int) bounds.getX(), (int) bounds.getY() - 88, (int) bounds.getWidth(), (int) bounds.getHeight());

				Widget background = widget.getDynamicChildren()[i * 9];
				int iconSize = 18;
				int x = background.getCanvasLocation().getX() + 188;
				int y1 = background.getCanvasLocation().getY() + 6 - 3;
				int y2 = background.getCanvasLocation().getY() + 23 - 3;
				if (setEffect1.iconId > 0) {
					BufferedImage image = itemManager.getImage(setEffect1.iconId);
					graphics.drawImage(image, x, y1, iconSize, iconSize, null, null);
				} else {
					BufferedImage image = spriteManager.getSprite(setEffect1.iconId * -1, 0);
					graphics.drawImage(image, x, y1, iconSize, iconSize, null, null);
				}

				if (setEffect2.iconId > 0) {
					BufferedImage image = itemManager.getImage(setEffect2.iconId);
					graphics.drawImage(image, x, y2, iconSize, iconSize, null, null);
				} else {
					BufferedImage image = spriteManager.getSprite(setEffect2.iconId * -1, 0);
					graphics.drawImage(image, x, y2, iconSize, iconSize, null, null);
				}
			}
			return null;
		}
	}

	@Subscribe
	public void onMenuEntryAdded(MenuEntryAdded e) {
		// ty memebeams!
		MenuEntry[] entries = client.getMenuEntries();
		if (config.swapViewEquip() && entries.length == 3 && e.getOption().equals("View") && client.getWidget(735, 35) != null && !client.getWidget(735, 35).isHidden()) {
			MenuEntry entry = entries[1];
			entries[1] = entries[2];
			entries[2] = entry;
			client.setMenuEntries(entries);
		}
	}
}
