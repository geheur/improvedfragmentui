package com.fragmentui;

import com.google.inject.Provides;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.MenuEntry;
import net.runelite.api.StructComposition;
import net.runelite.api.events.ClientTick;
import net.runelite.api.events.CommandExecuted;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ScriptPreFired;
import net.runelite.api.widgets.Widget;
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
import net.runelite.client.util.AsyncBufferedImage;

@Slf4j
@PluginDescriptor(
	name = "Improved Fragment UI"
)
public class FragmentUiPlugin extends Plugin
{
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

	int order = 0;
	Set<Integer> seenThisClientTick = new HashSet<>();
	String lastInput = "";

	Map<Integer, AsyncBufferedImage> setEffectImages = new HashMap<>();

	@Subscribe
		public void onClientTick(ClientTick e) {
		// Widget titleBarWidget = client.getWidget(735, 6);
		// for (int i = 0; i < 7; i++)
		// {
		// int slotValue = client.getVarbitValue(13395 + i);
		// log.info("" + slotValue);
		// relics.add("" + slotValue);
		// }
		// titleBarWidget.setText(titleBarWidget.getText() + " ");

		Widget equippedFragmentsWidget = client.getWidget(735, 35);
		if (equippedFragmentsWidget == null || equippedFragmentsWidget.isHidden()) return;
		// log.info("client tick");
		// client.getWidget(735, 9).setHidden(true);
		// client.getWidget(735, 11).setHidden(true);
		if (config.changeFragmentIcons())
		{
			changeFragmentIcons(equippedFragmentsWidget);
		}

		MenuEntry[] entries = client.getMenuEntries();
		if (config.swapViewEquip() && entries.length == 3 && entries[2].getOption().equals("View") && entries[1].getOption().equals("Equip")) {
			MenuEntry menuEntry = entries[2];
			entries[2] = entries[1];
			entries[1] = menuEntry;
		}
		client.setMenuEntries(entries);

		order = 0;
		seenThisClientTick.clear();

		if (config.filterFragments())
		{
			String input = client.getVarcStrValue(335);
			if (!input.equals(lastInput))
			{
				client.runScript(5751, 48168977, 48168978);
			}
			lastInput = input;
		}
//	client.runScript(5756, 48168968, 48168979, 48168969);
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
				equippedFragmentsWidget.getDynamicChildren()[i + 0].setText("LAKJSDLAJSKDLAKSDJALS");
				equippedFragmentsWidget.getDynamicChildren()[i + 3].setSpriteId(-1);
				equippedFragmentsWidget.getDynamicChildren()[i + 3].setText("LAKJSDLAJSKDLAKSDJALS");
			}
		}

		Widget widget = client.getWidget(735, 17);
		if (widget == null || widget.isHidden()) return;

		for (int i = 0; i < widget.getDynamicChildren().length; i += 9)
		{
			// log.info("{} {} {} {} {} {} {} {} {} {} {} {} {} {}", i, widget.getDynamicChildren()[i + 7].getText(), widget.getDynamicChildren()[i + 1].getSpriteId(), widget.getDynamicChildren()[i + 1].getSpriteId(), widget.getDynamicChildren()[i + 2].getSpriteId(), widget.getDynamicChildren()[i + 3].getSpriteId(), widget.getDynamicChildren()[i + 4].getSpriteId(), widget.getDynamicChildren()[i + 3].getSpriteId(), widget.getDynamicChildren()[i + 6].getSpriteId());
			int symbolId = widget.getDynamicChildren()[i + 3].getSpriteId();
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
				}
				widget.getDynamicChildren()[i + 2].setItemQuantityMode(0);
			}
			Widget fragmentBackground = widget.getDynamicChildren()[i + 1];
			Widget fragmentSymbol = widget.getDynamicChildren()[i + 2];

			// Widget fragmentName = widget.getDynamicChildren()[i + 7];
			// log.info(fragmentName.getText());
			// log.info(client.getVarbitValue(14402 + 1 + i));
			// fragmentName.setText(fragmentName.getText() + " xp: " + client.getVarbitValue(14402 + 1 + i));
		}
	}

	@Subscribe
		public void onScriptPreFired(ScriptPreFired e) {
		if (e.getScriptId() == 5756 || e.getScriptId() == 5751) {
//			log.info("logging " + e.getScriptId());
//			for (int i = 0; i < client.getIntStackSize(); i++)
//			{
//				log.info(i + " " + "" + client.getIntStack()[i]);
//			}
		}

		if (e.getScriptId() == 5752 && config.filterFragments()) {
			String input = client.getVarcStrValue(335);
			// log.info(input);
			String[] words = input.split(" ");

			int originalListPosition = client.getIntStack()[client.getIntStackSize() - 3];
			int struct = client.getIntStack()[client.getIntStackSize() - 2];
			if (seenThisClientTick.contains(struct)) {
				return;
			}
			seenThisClientTick.add(struct);

			StructComposition structComposition = client.getStructComposition(struct);
			String name = structComposition.getStringValue(1448);
			String description = structComposition.getStringValue(1449);
			// log.info("" + struct + " " + name);
			SetEffect setEffect1 = SetEffect.values()[structComposition.getIntValue(1459) - 1];
			SetEffect setEffect2 = SetEffect.values()[structComposition.getIntValue(1460) - 1];
			String setEffect1Name = setEffect1.name;
			String setEffect2Name = setEffect2.name;

			String relics = configManager.getRSProfileConfiguration("fragmentsearch", "tag_" + input);
			// log.info("relics: " + relics);
			if (relics != null) {
				String[] relicIds = relics.split(" ");
				boolean found = false;
				for (int i = 0; i < relicIds.length; i++) {
					// log.info(Integer.parseInt(relicIds[i]) + " " + (struct - 4038));
					if (Integer.parseInt(relicIds[i]) == originalListPosition + 1) {
						client.getIntStack()[client.getIntStackSize() - 1] = 0; // true
						client.getIntStack()[client.getIntStackSize() - 3] = order;
						found = true;
						break;
					} else {
						client.getIntStack()[client.getIntStackSize() - 1] = 1; // false
					}
				}
				if (!found) return;
			} else {
				for (String word : words) {
					if (name.toLowerCase().contains(word) || setEffect1Name.toLowerCase().contains(word) || setEffect2Name.toLowerCase().contains(word)) {
						client.getIntStack()[client.getIntStackSize() - 1] = 0; // true
						client.getIntStack()[client.getIntStackSize() - 3] = order;
						break;
					} else {
						client.getIntStack()[client.getIntStackSize() - 1] = 1; // false
						return;
					}
				}
			}

//			log.info("{}: ({}, {}), {} {} {} {} {} {} {}", name, setEffect1Name, setEffect2Name,
//				client.getIntStack()[client.getIntStackSize() - 1],
//				struct,
//				originalListPosition,
//				client.getIntStack()[client.getIntStackSize() - 4],
//				client.getIntStack()[client.getIntStackSize() - 5],
//				structComposition.getIntValue(1455)
//			);

			order++;
		}
	}


	@Subscribe
	public void onCommandExecuted(CommandExecuted e) {
		if (e.getCommand().equals("tagfragments")) {
			if (e.getArguments().length == 0) return;

			String tagName = e.getArguments()[0];
			List<String> relics = new ArrayList<>();
			for (int i = 0; i < 7; i++)
			{
				int slotValue = client.getVarbitValue(13395 + i);
//				log.info("" + slotValue);
				relics.add("" + slotValue);
			}
//			log.info("" + relics);
			configManager.setRSProfileConfiguration("fragmentsearch", "tag_" + tagName, String.join(" ", relics));
		}
	}

	@Override
	protected void startUp() throws Exception
	{
		overlayManager.add(new FragmentUiOverlay());
	}

	@Override
	protected void shutDown() throws Exception
	{
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
	icons.put(3902, 905); // Bottomless Quiver
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
	icons.put(3873, 6887); // Mother's Magic Fossils
	}

	Map<Integer, Integer> fragmentIdToStructId = new HashMap<>();
	{
	fragmentIdToStructId.put(1, 4038);
	fragmentIdToStructId.put(2, 4041);
	fragmentIdToStructId.put(3, 4039);
	fragmentIdToStructId.put(4, 4042);
	fragmentIdToStructId.put(5, 4040);
	fragmentIdToStructId.put(6, 4043);
	fragmentIdToStructId.put(7, 4054);
	fragmentIdToStructId.put(8, 4037);
	fragmentIdToStructId.put(9, 4053);
	fragmentIdToStructId.put(10, 4061);
	fragmentIdToStructId.put(11, 4047);
	fragmentIdToStructId.put(12, 4048);
	fragmentIdToStructId.put(13, 4089);
	fragmentIdToStructId.put(14, 4045);
	fragmentIdToStructId.put(15, 4046);
	fragmentIdToStructId.put(16, 4044);
	fragmentIdToStructId.put(17, 4083);
	fragmentIdToStructId.put(18, 4084);
	fragmentIdToStructId.put(19, 4085);
	fragmentIdToStructId.put(20, 4086);
	fragmentIdToStructId.put(21, 4049);
	fragmentIdToStructId.put(22, 4058);
	fragmentIdToStructId.put(23, 4051);
	fragmentIdToStructId.put(24, 4052);
	fragmentIdToStructId.put(25, 4050);
	fragmentIdToStructId.put(26, 4055);
	fragmentIdToStructId.put(27, 4056);
	fragmentIdToStructId.put(28, 4057);
	fragmentIdToStructId.put(29, 4059);
	fragmentIdToStructId.put(30, 4060);
	fragmentIdToStructId.put(31, 4073);
	fragmentIdToStructId.put(32, 4077);
	fragmentIdToStructId.put(33, 4078);
	fragmentIdToStructId.put(34, 4072);
	fragmentIdToStructId.put(35, 4067);
	fragmentIdToStructId.put(36, 4068);
	fragmentIdToStructId.put(37, 4064);
	fragmentIdToStructId.put(38, 4065);
	fragmentIdToStructId.put(39, 4074);
	fragmentIdToStructId.put(40, 4066);
	fragmentIdToStructId.put(41, 4079);
	fragmentIdToStructId.put(42, 4080);
	fragmentIdToStructId.put(43, 4087);
	fragmentIdToStructId.put(44, 4088);
	fragmentIdToStructId.put(45, 4062);
	fragmentIdToStructId.put(46, 4063);
	fragmentIdToStructId.put(47, 4075);
	fragmentIdToStructId.put(48, 4076);
	fragmentIdToStructId.put(49, 4081);
	fragmentIdToStructId.put(50, 4082);
	fragmentIdToStructId.put(51, 4069);
	fragmentIdToStructId.put(52, 4070);
	fragmentIdToStructId.put(53, 4071);
	}

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
				Integer structId = fragmentIdToStructId.get(slotValue);
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
				Integer structId = fragmentIdToStructId.get(fragmentId);
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
}
