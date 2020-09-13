package ui.classic;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import ui.UISettings;
import ui.shared.UIState;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public class RendererContainer {
	private static final Map<UIState, AbstractRenderer> GAME_RENDERERS = new EnumMap<>(UIState.class);

	private final RendererState state;
	private final UISettings settings;

	private final AbstractFrameRenderer frameRenderer;

	public RendererContainer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman, @Nonnull RendererState state,
		@Nonnull UISettings settings) {

		this.state = state;
		this.settings = settings;
		this.frameRenderer = createFrameRenderer(config, resman, settings);
	}

	public AbstractRenderer rendererFor(@Nonnull UIState uiState) {
		return GAME_RENDERERS.computeIfAbsent(uiState, this::createRenderer);
	}

	public AbstractFrameRenderer fameRenderer() {
		return frameRenderer;
	}

	private AbstractRenderer createRenderer(@Nonnull UIState uiState) {
		switch (uiState) {
			case BIGPIC:
				return new BigPicRenderer(state, settings, frameRenderer);
			case DUNGEON:
				return new DungeonRenderer(state, settings, frameRenderer);
			case OVERLAND:
				return new OverlandMapRenderer(state, settings, frameRenderer);
			case SPACE:
				return new SpaceTravelRenderer(state, settings, frameRenderer);
			case STORY:
				return new StoryRenderer(state, settings, frameRenderer);
			case TITLE:
				return new TitleRenderer(state, settings, frameRenderer);
			default:
				throw new IllegalArgumentException("Unknown UIState: " + uiState);
		}
	}

	public static AbstractFrameRenderer createFrameRenderer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman,
		@Nonnull UISettings settings) {

		switch (config.getFrameType()) {
			case FRAME:
				return new FrameRendererFrames(config, resman, settings);
			case SYMBOLS:
				return new FrameRendererSymbols(config, resman, settings);
			case SYMBOLS_PORTRAIT_FRAME:
				return new FrameRendererPortraitFrame(config, resman, settings);
			default:
				throw new IllegalArgumentException("Unknown FrameType: " + config.getFrameType());
		}
	}
}
