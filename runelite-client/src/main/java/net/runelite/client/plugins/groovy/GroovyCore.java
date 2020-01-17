package net.runelite.client.plugins.groovy;

import com.google.common.base.Strings;
import com.google.inject.Binder;
import com.google.inject.Provides;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.menus.MenuManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.plugins.PluginType;
import net.runelite.client.plugins.groovy.interfaces.GroovyContext;
import net.runelite.client.plugins.groovy.ui.GroovyPanel;
import net.runelite.client.plugins.groovy.ui.GroovyScriptPanel;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;

import static net.runelite.client.plugins.groovy.GroovyScriptsParse.NEWLINE_SPLITTER;

/**
 * Authors fred
 */
@PluginDescriptor(
	name = "Groovy Core",
	description = "Allows for Groovy scripting at runtime",
	tags = {"fred", "shell", "scripting", "groovy"},
	type = PluginType.FRED
)
@Singleton
@Slf4j
public class GroovyCore extends Plugin
{
	@Inject
	private ScheduledExecutorService executorService;

	private NavigationButton navButton;

	@Setter(AccessLevel.PACKAGE)
	private GroovyPanel panel;

	@Inject
	private ClientToolbar clientToolbar;

	@Inject
	private Client client;

	@Inject
	private EventBus eventBus;

	@Inject
	private MenuManager menuManager;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private GroovyConfig config;

	@Inject
	private GroovyManager groovyManager;

	//settings
	@Getter(AccessLevel.PUBLIC)
	private String groovyRoot;

	@Getter(AccessLevel.PUBLIC)
	private String groovyScripts;

	@Provides
	GroovyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GroovyConfig.class);
	}

	@Override
	public void configure(Binder binder)
	{
		log.debug("configure callback");
	}

	private void loadScriptsFromConfig()
	{
		this.groovyScripts = config.groovyScripts();
		groovyManager.clear();
		if (!Strings.isNullOrEmpty(groovyScripts))
		{
			final StringBuilder sb = new StringBuilder();

			for (String str : groovyScripts.split("\n"))
			{
				if (!str.startsWith("//"))
				{
					sb.append(str).append("\n");
				}
			}

			@SuppressWarnings("UnstableApiUsage") final Map<String, String> split = NEWLINE_SPLITTER.withKeyValueSeparator(" | ").split(sb);
			for (Map.Entry<String, String> entry : split.entrySet())
			{
				final String name = entry.getKey().trim();
				final boolean enabled = Boolean.parseBoolean(entry.getValue().trim());
				final String resolvedName = groovyRoot + name;

				GroovyContext context = new GroovyContext(name, resolvedName, client, eventBus, menuManager, overlayManager);

//				int uuid = groovyManager.registerScript(context);
//				groovyManager.enablePlugin(uuid, enabled);
			}
		}
	}

	private final Supplier<List<GroovyScriptPanel>> scriptEntrySupplier = () -> groovyManager.getScriptPanels();

	@Override
	protected void startUp()
	{
		panel = new GroovyPanel(this, scriptEntrySupplier);
		final BufferedImage icon = loadImage("panel_icon");
		navButton = NavigationButton.builder()
			.tooltip("Groovy")
			.icon(icon)
			.priority(2)
			.panel(panel)
			.build();
		clientToolbar.addNavigation(navButton);

		this.groovyRoot = config.groovyRoot();

		loadScriptsFromConfig();
		addSubscriptions();
		this.updateList();
	}

	BufferedImage loadImage(String path)
	{
		return ImageUtil.getResourceStreamFromClass(getClass(), "/net/runelite/client/plugins/groovy/" + path + ".png");
	}

	@Override
	protected void shutDown()
	{
		groovyManager.clear();
		eventBus.unregister(this);
		clientToolbar.removeNavigation(navButton);
	}

	private void addSubscriptions()
	{
		eventBus.subscribe(ConfigChanged.class, this, this::onConfigChanged);
	}

	private void onConfigChanged(ConfigChanged event)
	{
		if (!event.getGroup().equals("groovy"))
		{
			return;
		}
		if (event.getKey().equalsIgnoreCase("groovyRoot") || event.getKey().equalsIgnoreCase("groovyScripts"))
		{
			loadScriptsFromConfig();
		}
	}

	void updateList()
	{
		executorService.submit(panel::rebuildScriptPanels);
	}
}
