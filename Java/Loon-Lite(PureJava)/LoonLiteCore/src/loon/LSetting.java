/**
 * Copyright 2008 - 2015 The Loon Game Engine Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * @project loon
 * @author cping
 * @email：javachenpeng@yahoo.com
 * @version 0.5
 */
package loon;

import loon.font.IFont;
import loon.utils.DPI;
import loon.utils.MathUtils;
import loon.utils.NumberUtils;
import loon.utils.Resolution;
import loon.utils.timer.Duration;

/**
 * LGame的基础配置类,游戏初始化属性由此产生
 */
public class LSetting {

	/**
	 * 若此项不为-1,则loon的Display类中LTimerContext在被传参时,以此数值替换动态计算出的paint刷新数值(也就是强制锁值),
	 * 默认单位是毫秒，比如锁定1/60帧就是(long)((1f/60f) * 1000)
	 */
	public long fixedPaintLoopTime = -1;

	/**
	 * 若此项不为-1,则loon的Display类中LTimerContext在被传参时,以此数值替换动态计算出的update刷新数值(也就是强制锁值),
	 * 默认单位是毫秒，比如锁定1/60帧就是(long)((1f/60f) * 1000)
	 */
	public long fixedUpdateLoopTime = -1;

	/**
	 * 默认游戏字体设置
	 */
	public IFont defaultGameFont;

	/**
	 * 默认log字体设置
	 */
	public IFont defaultLogFont;

	/**
	 * loon自带图的存放路径和文件前缀(默认assets起,不用写)
	 */
	public String systemImgPath = "loon_";

	/**
	 * Loon自带的模拟按键的缩放比率(Screen实现EmulatorListener接口自动出现,8个按钮)
	 */
	public float emulatorScale = 1f;

	/**
	 * 如果此项为true,则Loon会检查resize缩放行为,原本宽高比例是横屏，改成竖屏，或者竖屏改成横屏的resize将不被允许
	 */
	public boolean isCheckResize = false;

	/**
	 * 如果此项为true,则游戏窗体缩放时直接缩放原始画面大小,否则仅仅传递缩放数据,需要自行编码处理具体缩放内容
	 */
	public boolean isSimpleScaling = true;
	
	/**
	 * 如果此项为true,则游戏窗体可以缩放大小
	 */
	public boolean isAllowScreenResizabled = true;

	/**
	 * 此项为true,选择窗体关闭时直接退出
	 */
	public boolean isCloseOnAppExit = true;

	/**
	 * 如果此项为true,则Loon中的缓动动画会和图像渲染同步(为false时缓动刷新次数会比画面渲染次数少),true时缓动动画会更加流畅,
	 * <p>
	 * 但是缓动资源较多则可能延迟画面渲染(因为都卡在一起执行了)
	 */
	public boolean isSyncTween = false;

	/**
	 * 若此处true,则fps,memory以及sprite数量之类数据强制显示
	 */
	public boolean isDebug = false;

	/**
	 * 是否显示viewlog,此项为true时,log信息也将同时打印到游戏窗体中
	 */
	public boolean isDisplayLog = false;

	/**
	 * 是否显示consolelog,此项为false时,关闭所有后台log信息
	 */
	public boolean isConsoleLog = true;

	/**
	 * 是否显示FPS帧率
	 */
	public boolean isFPS = false;

	/**
	 * 是否显示游戏内存消耗
	 */
	public boolean isMemory = false;

	/**
	 * 是否显示精灵与桌面组件数量
	 */
	public boolean isSprites = false;

	/**
	 * 是否显示logo（替换logo使用logoPath指定地址）
	 */
	public boolean isLogo = false;

	/**
	 * 生成系统默认的LFont时,是否使用剪切生成
	 */
	public boolean useTrueFontClip = true;
	/**
	 * 此项为true时采用全屏刷新,为false则屏幕不会自动刷新
	 */
	public boolean allScreenRefresh = true;
	
	public Class<?> mainClass = null;

	/**
	 * 帧率
	 */
	public int fps = 60;

	/**
	 * 游戏画面实际宽度
	 */
	public int width = 480;

	/**
	 * 游戏画面实际高度
	 */
	public int height = 320;

	/**
	 * 游戏画面缩放大小,假如原始画面大小480x320,下列项为640x480,则会拉伸画布,缩放到640x480显示（不需要则维持在-1即可）
	 */
	public int width_zoom = -1;

	public int height_zoom = -1;

	/**
	 * 是否全屏
	 */
	public boolean fullscreen = false;

	/**
	 * 是否使用虚拟触屏按钮(针对非手机平台)
	 */
	public boolean emulateTouch = false;

	/**
	 * 仅对JavaSE环境有效,不为-1时对应的按键触发后,窗体停止活动
	 */
	public int activationKey = -1;

	/**
	 * 当前游戏或应用名
	 */
	public String appName = "Loon";

	/**
	 * 使用的初始化logo
	 */
	public String logoPath = "loon_logo.png";

	/**
	 * 当前字体名
	 */
	public String fontName = "Dialog";

	/**
	 * 当前应用版本号
	 */
	public String version = LSystem.UNKNOWN;

	/**
	 * 允许注销纹理(为false所有纹理都不被注销)
	 */
	public boolean disposeTexture = true;

	/**
	 * 保存注入纹理的像素(为false不保存)
	 */
	public boolean saveTexturePixels = true;

	/**
	 * 此项为true时,drag与move事件全游戏无效
	 */
	public boolean notAllowDragAndMove = false;

	/**
	 * 锁定全部Touch事件,此项为true时,Loon中所有触屏(鼠标)事件不生效
	 */
	public boolean lockAllTouchEvent = false;

	/**
	 * 初始化游戏时传参用，默认无数据
	 */
	public String[] args = new String[] { "" };
	
	/**
	 * 强制转化图片类型
	 */
	public boolean convertImagesOnLoad = true;

	/**
	 * 复制setting设置到自身
	 *
	 * @param setting
	 */
	public void copy(LSetting setting) {
		this.isSyncTween = setting.isSyncTween;
		this.isFPS = setting.isFPS;
		this.isLogo = setting.isLogo;
		this.isCheckResize = setting.isCheckResize;
		this.isAllowScreenResizabled = setting.isAllowScreenResizabled;
		this.isConsoleLog = setting.isConsoleLog;
		this.disposeTexture = setting.disposeTexture;
		this.fps = setting.fps;
		this.width = setting.width;
		this.height = setting.height;
		this.width_zoom = setting.width_zoom;
		this.height_zoom = setting.height_zoom;
		this.fullscreen = setting.fullscreen;
		this.emulateTouch = setting.emulateTouch;
		this.activationKey = setting.activationKey;
		this.saveTexturePixels = setting.saveTexturePixels;
		this.appName = setting.appName;
		this.logoPath = setting.logoPath;
		this.fontName = setting.fontName;
		this.version = setting.version;
		this.fixedPaintLoopTime = setting.fixedPaintLoopTime;
		this.fixedUpdateLoopTime = setting.fixedUpdateLoopTime;
		this.useTrueFontClip = setting.useTrueFontClip;
		this.emulatorScale = setting.emulatorScale;
		this.notAllowDragAndMove = setting.notAllowDragAndMove;
		this.lockAllTouchEvent = setting.lockAllTouchEvent;
		this.convertImagesOnLoad = setting.convertImagesOnLoad;
		this.allScreenRefresh = setting.allScreenRefresh;
		this.args = setting.args;
	}

	/**
	 * 全局的log显示用字体,不设置则默认使用LFont贴图本地字体
	 */
	public void setSystemLogFont(IFont font) {
		defaultLogFont = font;
	}

	/**
	 * 全局的游戏画面用字体,不设置则默认使用LFont贴图本地字体
	 */
	public void setSystemGameFont(IFont font) {
		defaultGameFont = font;
	}

	/**
	 * loon中一切字体的统一设置
	 */
	public void setSystemGlobalFont(IFont font) {
		setSystemLogFont(font);
		setSystemGameFont(font);
	}

	/**
	 * 锁死paint刷新为1/60帧
	 */
	public LSetting fixedPaintTime() {
		fixedPaintTime(1f / 60f);
		return this;
	}

	public LSetting fixedPaintTime(float time) {
		this.fixedPaintLoopTime = Duration.ofS(time);
		return this;
	}

	/**
	 * 锁死update刷新为3.5/60帧
	 */
	public LSetting fixedUpdateTime() {
		fixedPaintTime(3.5f / 60f);
		return this;
	}

	public LSetting fixedUpdateTime(float time) {
		this.fixedUpdateLoopTime = Duration.ofS(time);
		return this;
	}

	public boolean landscape() {
		return this.height < this.width;
	}

	public boolean portrait() {
		return this.height >= this.width;
	}

	public LSetting updateScale(float max) {
		return updateScale(width, height, max);
	}

	public LSetting updateScale(float w, float h, float max) {
		if (w == 0 || h == 0) {
			return this;
		}
		float nWidth;
		float nHeight;
		final float ratio = w / h;
		final float newHeight = w / ratio;
		final float newWidth = h * ratio;
		if (newWidth == newHeight) {
			nWidth = max;
			nHeight = max;
		} else if (newWidth > newHeight) {
			nWidth = max;
			nHeight = h / w * max;
		} else {
			nHeight = max;
			nWidth = w / h * max;
		}
		this.width = MathUtils.floor(w);
		this.height = MathUtils.floor(h);
		this.width_zoom = MathUtils.floor(nWidth);
		this.height_zoom = MathUtils.floor(nHeight);
		updateScale();
		return this;
	}

	public LSetting updateScale() {
		width = MathUtils.divTwoAbs(width);
		height = MathUtils.divTwoAbs(height);
		width_zoom = MathUtils.divTwoAbs(width_zoom);
		height_zoom = MathUtils.divTwoAbs(height_zoom);
		if (scaling()) {
			LSystem.setScaleWidth((float) width_zoom / (float) width);
			LSystem.setScaleHeight((float) height_zoom / (float) height);
			LSystem.setSize(width, height);
			if (LSystem.getProcess() != null) {
				LSystem.getProcess().resize(width, height);
			}
		} else {
			LSystem.setScaleWidth(1f);
			LSystem.setScaleHeight(1f);
			LSystem.setSize(width, height);
			if (LSystem.getProcess() != null) {
				LSystem.getProcess().resize(width, height);
			}
		}
		return this;
	}

	public DPI getDpi() {
		return new Resolution(width, height).compareDPI(new Resolution(width_zoom, height_zoom));
	}

	public boolean scaling() {
		return this.width_zoom > 0 && this.height_zoom > 0
				&& (this.width_zoom != this.width || this.height_zoom != this.height);
	}

	public int getShowWidth() {
		return this.width_zoom > 0 ? this.width_zoom : this.width;
	}

	public int getShowHeight() {
		return this.height_zoom > 0 ? this.height_zoom : this.height;
	}

	/**
	 * 判断设备是否宽屏
	 *
	 * @return
	 */
	public boolean wideScreen() {
		return NumberUtils.compare(getShowWidth() / getShowHeight(), 1.777777f) == 0;
	}

}
