package ui.classic;

import static io.vavr.API.Map;

import javax.annotation.Nonnull;

import io.vavr.collection.Map;

import ui.UISettings;
import ui.shared.UIState;
import ui.shared.resource.UIResourceConfiguration;
import ui.shared.resource.UIResourceManager;

public class RendererContainer {
	private Map<UIState, AbstractStateRenderer> GAME_RENDERERS = Map();
	private Map<Class<? extends AbstractDialogRenderer>, AbstractDialogRenderer> DIALOG_RENDERERS = Map();

	private final RendererState state;
	private final UISettings settings;
	private final UIResourceManager resman;
	private final UIResourceConfiguration config;

	private final AbstractFrameRenderer frameRenderer;
	private final TextInputRenderer inputRenderer;

	public RendererContainer(@Nonnull UIResourceConfiguration config, @Nonnull UIResourceManager resman,
		@Nonnull RendererState state, @Nonnull UISettings settings) {

		this.config = config;
		this.resman = resman;
		this.state = state;
		this.settings = settings;
		this.frameRenderer = createFrameRenderer(config, resman, settings);
		this.inputRenderer = new TextInputRenderer(settings, resman, frameRenderer);
	}

	public AbstractStateRenderer rendererFor(@Nonnull UIState uiState) {
		if (!GAME_RENDERERS.containsKey(uiState))
			GAME_RENDERERS = GAME_RENDERERS.put(uiState, createRenderer(uiState));
		return GAME_RENDERERS.get(uiState).get();
	}

	public AbstractDialogRenderer rendererFor(@Nonnull AbstractDialogState dialogState) {
		final Class<? extends AbstractDialogRenderer> rendererClass = dialogState.getRendererClass();
		if (!DIALOG_RENDERERS.containsKey(rendererClass))
			DIALOG_RENDERERS = DIALOG_RENDERERS.put(rendererClass, createRenderer(rendererClass));
		return DIALOG_RENDERERS.get(rendererClass).get();
	}

	public AbstractFrameRenderer frameRenderer() {
		return frameRenderer;
	}

	public TextInputRenderer inputRenderer() {
		return inputRenderer;
	}

	private AbstractStateRenderer createRenderer(@Nonnull UIState uiState) {
		switch (uiState) {
			case BIGPIC:
				return new BigPicRenderer(state, settings, resman, frameRenderer);
			case DUNGEON:
				return new DungeonRenderer(state, settings, resman, frameRenderer);
			case OVERLAND:
				return new OverlandMapRenderer(state, settings, resman, frameRenderer);
			case SPACE:
				return new SpaceTravelRenderer(state, settings, resman, frameRenderer);
			case STORY:
				return new StoryRenderer(state, settings, resman, frameRenderer);
			case TITLE:
				return new TitleRenderer(state, settings, resman, frameRenderer);
			default:
				throw new IllegalArgumentException("Unknown UIState: " + uiState);
		}
	}

	private AbstractDialogRenderer createRenderer(@Nonnull Class<? extends AbstractDialogRenderer> clazz) {
		if (ProgramMenuRenderer.class.equals(clazz)) {
			return new ProgramMenuRenderer(settings, resman, frameRenderer);
		}
		if (CharacterSheetRenderer.class.equals(clazz)) {
			return new CharacterSheetRenderer(config, settings, resman, frameRenderer);
		}
		throw new IllegalArgumentException("Unknown dialog renderer class: " + clazz.getName());
	}

	public static AbstractFrameRenderer createFrameRenderer(@Nonnull UIResourceConfiguration config,
		@Nonnull UIResourceManager resman, @Nonnull UISettings settings) {

		switch (config.getFrameType()) {
			case FRAME:
				return new FrameRendererFrames(config, resman, settings);
			case SYMBOLS:
				return new FrameRendererSymbols(config, resman, settings);
			case SYMBOLS_PORTRAIT_FRAME:
				return new FrameRendererPortraitFrame(config, resman, settings);
			case SYMBOLS_SPACE:
				return new FrameRendererSpace(config, resman, settings);
			default:
				throw new IllegalArgumentException("Unknown FrameType: " + config.getFrameType());
		}
	}
}
