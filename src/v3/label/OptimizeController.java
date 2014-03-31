package v3.label;

/**
 * 本系统的常量管理
 */
public class OptimizeController {
	/**
	 * 优化的状态
	 */
	public static enum OptimizeState {
		/**
		 * 未进入优化的状态
		 */
		NOT_IN_STATE,
		/**
		 * 设置状态
		 * 
		 */
		SET_STATE, /**
		 * 优化状态
		 */
		OPTIMIZE_STATE;
	}

	/**
	 * 当前状态
	 */
	private OptimizeState state;

	public OptimizeController() {
		state = OptimizeState.NOT_IN_STATE;
		//		state = OptimizeState.OPTIMIZE_STATE;
	}

	/**
	 * 得到当前状态
	 */
	public OptimizeState getState() {
		return state;
	}

	/**
	 * 设置当前状态
	 */
	public void setState(OptimizeState state) {
		this.state = state;
	}
}
