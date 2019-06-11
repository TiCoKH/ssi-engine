package engine.input;

import javax.annotation.Nonnull;

import engine.Engine;
import engine.EngineInputAction;

public class MovementHandler implements InputHandler {
	private Mode mode;

	public void setMode(Mode mode) {
		this.mode = mode;
	}

	@Override
	public void handle(Engine engine, EngineInputAction action) {
		mode.handle(engine, action);
	}

	public enum Mode implements InputHandler {
		DUNGEON(new DungeonMovementHandler()), //
		OVERLAND(new OverlandMovementHandler()), //
		SPACE(new SpaceMovementHandler());

		private InputHandler detailHandler;

		private Mode(@Nonnull InputHandler detailHandler) {
			this.detailHandler = detailHandler;
		}

		@Override
		public void handle(Engine engine, EngineInputAction action) {
			detailHandler.handle(engine, action);
		}
	}
}
