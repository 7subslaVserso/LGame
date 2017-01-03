package loon.action.page;

import loon.Screen;
import loon.Screen.PageMethod;
import loon.utils.timer.EaseTimer;

public class ScreenSwitch {

	private Screen _target;

	private Screen _source;

	private final EaseTimer _easeTimer;

	private final PageMethod _mode;

	private float _targetProgress, _sourceProgress;

	private BasePage _targetPage, _sourcePage;

	public ScreenSwitch(PageMethod mode, Screen dst, Screen src) {
		this(mode, dst, src, 1f);
	}

	public ScreenSwitch(Screen dst, Screen src, BasePage page1, BasePage page2,
			float duration) {
		this._mode = PageMethod.Unkown;
		this._target = dst;
		this._source = src;
		this._easeTimer = new EaseTimer(duration);
		this._targetPage = page1;
		this._sourcePage = page2;
	}

	public ScreenSwitch(PageMethod mode, Screen dst, Screen src, float duration) {
		this._mode = mode;
		this._target = dst;
		this._source = src;
		this._easeTimer = new EaseTimer(duration);
		switch (mode) {
		case Accordion:
			this._targetPage = new AccordionPage();
			this._sourcePage = new AccordionPage();
			break;
		case BackToFore:
			this._targetPage = new BTFPage();
			this._sourcePage = new BTFPage();
			break;
		case CubeIn:
			this._targetPage = new CubeInPage();
			this._sourcePage = new CubeInPage();
			break;
		case Depth:
			this._targetPage = new DepthPage();
			this._sourcePage = new DepthPage();
			break;
		case Fade:
			this._targetPage = new FadePage();
			this._sourcePage = new FadePage();
			break;
		case RotateDown:
			this._targetPage = new RotateDownPage();
			this._sourcePage = new RotateDownPage();
			break;
		case RotateUp:
			this._targetPage = new RotateUpPage();
			this._sourcePage = new RotateUpPage();
			break;
		case Stack:
			this._targetPage = new StackPage();
			this._sourcePage = new StackPage();
			break;
		case ZoomIn:
			this._targetPage = new ZoomInPage();
			this._sourcePage = new ZoomInPage();
			break;
		case ZoomOut:
			this._targetPage = new ZoomOutPage();
			this._sourcePage = new ZoomOutPage();
			break;
		default:
			break;
		}
	}

	public boolean isCompleted() {
		return _easeTimer.isCompleted();
	}

	public void update(long elapsedTime) {
		_easeTimer.update(elapsedTime);
		_targetProgress = _easeTimer.getProgress();
		_sourceProgress = 1f - _targetProgress;
		if (_target != null) {
			_targetPage.onTransform(_target, _targetProgress);
		}
		if (_source != null) {
			_sourcePage.onTransform(_source, _sourceProgress);
		}
		if (_easeTimer.isCompleted()) {
			if (_target != null) {
				_targetPage.resetTransform(_target);
			}
			if (_source != null) {
				_sourcePage.resetTransform(_source);
			}
		}
	}

	public EaseTimer getEaseTimer() {
		return _easeTimer;
	}

	public PageMethod getMode() {
		return _mode;
	}
}
