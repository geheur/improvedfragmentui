package com.fragmentui;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(FragmentUiPlugin.GROUP_NAME)
public interface FragmentUiConfig extends Config
{
	@ConfigItem(
		keyName = "tutorial",
		name = "Instructions",
		description = "Instructions for using the plugin.",
		position = 0
	)
	default String tutorial()
	{
		return "=== Filtering ===\nYou can filter the fragment list by typing into the game's chatbox. You can search for fragment names or set effect names.\n\n=== Reordering ===\nYou can change the order of the list by dragging a fragment's icon onto another (you cannot drag fragments that are equipped, nor can you reorder it while the list is filtered, sorry). You can also revert your sort to the default with the \"::fragui sort reset\" command, or set it to an alphabetical list with \"::fragui sort alpha\".\n\n=== Presets ===\nYou can create a preset with \"::fragui preset presetName\", which will create a preset of your equipped fragments. When you search for exactly the name of the preset, the fragment list will be filtered to the fragments in that preset. You can delete a preset with \"::fragui delete presetName\". You can list presets with \"::fragui list\"";
	}


	@ConfigItem(
		keyName = "showSetEffectIcons",
		name = "Set Effect Icons",
		description = "Show icons for set effects next to relics in the menu.",
		position = 1
	)
	default boolean showSetEffectIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "changeFragmentIcons",
		name = "Fragment Icons",
		description = "Show more recognizable icons for fragments.",
		position = 2
	)
	default boolean changeFragmentIcons()
	{
		return true;
	}

	@ConfigItem(
		keyName = "swapViewEquip",
		name = "Swap (Un)Equip",
		description = "Swaps Equip and Unequip for fragments in the list.",
		position = 3
	)
	default boolean swapViewEquip()
	{
		return true;
	}

	@ConfigItem(
		keyName = "filterFragments",
		name = "Fragment filtering",
		description = "Filter the fragment list using what's in the chatbox.",
		position = 4
	)
	default boolean filterFragments()
	{
		return true;
	}
}
