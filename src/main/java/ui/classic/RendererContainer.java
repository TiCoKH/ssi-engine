package ui.classic;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ui.UIResourceConfiguration;
import ui.UIResourceManager;
import ui.UIResources;
import ui.UISettings;
import ui.UIState;

public class RendererContainer {
	private static final Map<UIState, AbstractRenderer> GAME_RENDERERS = new EnumMap<>(UIState.class);

	private final UIResources resources;
	private final UISettings settings;

	private final FrameRenderer frameRenderer;

	public RendererContainer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman, @Nonnull UIResources resources,
		@Nonnull UISettings settings) {

		this.resources = resources;
		this.settings = settings;
		this.frameRenderer = new FrameRenderer(config, resman, settings);
	}

	public AbstractRenderer rendererFor(@Nonnull UIState uiState) {
		return GAME_RENDERERS.computeIfAbsent(uiState, this::createRenderer);
	}

	public FrameRenderer fameRenderer() {
		return frameRenderer;
	}

	private AbstractRenderer createRenderer(@Nonnull UIState uiState) {
		switch (uiState) {
			case BIGPIC:
				return new BigPicRenderer(resources, settings, frameRenderer);
			case DUNGEON:
				return new DungeonRenderer(resources, settings, frameRenderer);
			case OVERLAND:
				return new OverlandMapRenderer(resources, settings, frameRenderer);
			case SPACE:
				return new SpaceTravelRenderer(resources, settings, frameRenderer);
			case STORY:
				return new StoryRenderer(resources, settings, frameRenderer);
			case TITLE:
				return new TitleRenderer(resources, settings, frameRenderer);
			default:
				throw new IllegalArgumentException("Unknown UIState: " + uiState);
		}
	}
}
