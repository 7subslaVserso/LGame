/**
 * Copyright 2008 - 2019 The Loon Game Engine Authors
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
package loon.fx;

import java.util.Optional;

import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCombination;
import javafx.scene.transform.Scale;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import loon.LGame;
import loon.LSetting;
import loon.LSysException;
import loon.LazyLoading;
import loon.Platform;
import loon.events.KeyMake;
import loon.events.SysInput;
import loon.utils.Resolution;
import loon.utils.StringUtils;

public class JavaFXApplication extends Application implements Platform {

	static class InitDataCached {

		protected LazyLoading.Data lazyData;

		protected JavaFXSetting appSetting;

		protected Class<?> mainClass;
	}
	
	private final static InitDataCached initData = new InitDataCached();

	public static void launchFX(Loon app, JavaFXSetting setting, LazyLoading.Data lazy, String[] args) {
		initData.lazyData = lazy;
		initData.mainClass = app.getClass();
		initData.appSetting = setting;
		if (initData.appSetting.args == null) {
			initData.appSetting.args = args;
		}
		Application.launch(JavaFXApplication.class, args);
	}

	private DoubleProperty scaledWidth = new SimpleDoubleProperty();
	private DoubleProperty scaledHeight = new SimpleDoubleProperty();
	private DoubleProperty scaleRatioX = new SimpleDoubleProperty();
	private DoubleProperty scaleRatioY = new SimpleDoubleProperty();

	private double windowBorderWidth = 0d;
	private double windowBorderHeight = 0d;

	protected JavaFXGame game;

	protected Scene fxScene;

	protected LSetting appSetting;

	protected LazyLoading.Data lazyData;

	protected JavaFXResizeCanvas fxCanvas;

	public JavaFXApplication() {
		this.lazyData = initData.lazyData;
		this.appSetting = initData.appSetting;
		this.appSetting.mainClass = initData.mainClass;
		if (appSetting.fullscreen) {
			Rectangle2D viewRect = null;
			if (appSetting.fullscreen && JavaFXGame.isJavaFXDesktop()) {
				viewRect = Screen.getPrimary().getBounds();
			} else {
				viewRect = Screen.getPrimary().getVisualBounds();
			}
			float width = (float) viewRect.getWidth();
			float height = (float) viewRect.getHeight();
			if ((width > viewRect.getWidth()) || (height > viewRect.getHeight())) {
				float extraMargin = 25f;
				float ratio = width / height;
				for (int i = 0; i < viewRect.getWidth(); i++) {
					if (width / ratio <= viewRect.getHeight()) {
						width = i - extraMargin;
						height = i / ratio;
						break;
					}
				}
			}
			appSetting.width_zoom = (int) width;
			appSetting.height_zoom = (int) height;
		}
		this.game = new JavaFXGame(this, appSetting);
	}

	@Override
	public void stop() throws Exception {
		if (game.setting.isCloseOnAppExit) {
			super.stop();
			this.game.shutdown();
		}
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		final boolean desktop = (JavaFXGame.isJavaFXDesktop() || game.isDesktop());

		// 如果javafx在android或ios上跑强制全屏
		if (game.isMobile() || appSetting.fullscreen) {
			primaryStage.initStyle(StageStyle.TRANSPARENT);
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
		}

		primaryStage.setTitle(game.setting.appName);

		if (game.setting.fullscreen && desktop) {
			primaryStage.setFullScreen(true);
			primaryStage.setMaximized(true);
			primaryStage.setFullScreenExitHint("");
		}
		try {
			game.register(lazyData.onScreen());
		} catch (Exception e) {
			e.printStackTrace();
			throw new LSysException(e.getMessage());
		}
		game.reset();
		float newWidth = game.setting.getShowWidth();
		float newHeight = game.setting.getShowHeight();

		Group group = new Group();

		fxCanvas = game.getFxCanvas();

		if (fxCanvas == null) {
			fxCanvas = new JavaFXResizeCanvas(game.graphics(), newWidth, newHeight);
		}
		fxCanvas.setCache(false);

		GraphicsContext ctx = fxCanvas.getGraphicsContext2D();

		Paint paint = ctx.getFill();
		ctx.setFill(Color.BLACK);
		ctx.fillRect(0, 0, newWidth, newHeight);
		ctx.setFill(paint);

		group.getChildren().add(fxCanvas);

		primaryStage.setScene(createScene(group, newWidth, newHeight, desktop));
		primaryStage.setOnCloseRequest(e -> {
			if (game.setting.isCloseOnAppExit) {
				game.shutdown();
			} else {
				e.consume();
			}
		});

		if (game.setting instanceof JavaFXSetting) {
			JavaFXSetting fxsetting = ((JavaFXSetting) game.setting);
			primaryStage.setResizable(fxsetting.resizable);
			if (fxsetting.iconPaths != null) {
				String[] paths = fxsetting.iconPaths;
				for (String path : paths) {
					if (!StringUtils.isEmpty(path)) {
						try {
							JavaFXImage img = (JavaFXImage) game.assets().getImageSync(path);
							if (img != null) {
								primaryStage.getIcons().add(img.fxImage());
							}
						} catch (Exception e) {
							systemLog(e.getMessage());
						}
					}
				}
			}
		}
		if (!game.setting.isAllowScreenResizabled && desktop) {
			primaryStage.setResizable(false);
		}
		primaryStage.show();

		windowBorderWidth = primaryStage.getWidth() - scaledWidth.getValue();
		windowBorderHeight = primaryStage.getHeight() - scaledHeight.getValue();

		if (windowBorderHeight < 0.5 && JavaFXGame.isLinux()) {
			windowBorderHeight = 35d;
		}

		scaledWidth.bind(primaryStage.widthProperty().subtract(windowBorderWidth));
		scaledHeight.bind(primaryStage.heightProperty().subtract(windowBorderHeight));

		scaleRatioX.bind(scaledWidth.divide(newWidth));
		scaleRatioY.bind(scaledHeight.divide(newHeight));
		fxScene.getRoot().prefWidth(scaledWidth.doubleValue());
		fxScene.getRoot().prefHeight(scaledHeight.doubleValue());

		Scale scale = new Scale();
		scale.xProperty().bind(scaleRatioX);
		scale.yProperty().bind(scaleRatioY);

		fxScene.getRoot().getTransforms().setAll(scale);

	}

	protected Scene createScene(Group group, float width, float height, boolean desktop) {
		scaledWidth.set(width);
		scaledHeight.set(height);
		scaleRatioX.set(scaledWidth.getValue() / width);
		scaleRatioY.set(scaledHeight.getValue() / height);
	
		return (this.fxScene = new Scene(group, width, height, false, SceneAntialiasing.BALANCED));
	}

	public static void systemLog(String message) {
		System.out.println(message);
	}

	@Override
	public int getContainerWidth() {
		if (fxScene != null) {
			return (int) fxScene.getWidth();
		}
		if (fxCanvas != null) {
			return (int) fxCanvas.getWidth();
		}
		return game.setting != null ? game.setting.getShowWidth() : 0;
	}

	@Override
	public int getContainerHeight() {
		if (fxScene != null) {
			return (int) fxScene.getHeight();
		}
		if (fxCanvas != null) {
			return (int) fxCanvas.getHeight();
		}
		return game.setting != null ? game.setting.getShowHeight() : 0;
	}

	@Override
	public void close() {
		if (game != null) {
			game.close();
		}
		System.exit(-1);
	}

	@Override
	public Orientation getOrientation() {
		if (getContainerHeight() > getContainerWidth()) {
			return Orientation.Portrait;
		} else {
			return Orientation.Landscape;
		}
	}

	public static float dpiScale() {
		return Resolution.convertDPIScale(dpi());
	}

	public static int dpi() {
		return (int) Screen.getPrimary().getDpi();
	}

	public static float ppiY() {
		return dpi();
	}

	public static float ppcX() {
		return dpi() / 2.54f;
	}

	public static boolean isLowResolution() {
		return dpi() < 120;
	}

	@Override
	public void sysText(final SysInput.TextEvent event, final KeyMake.TextType textType, final String label,
			final String initVal) {
		if (game == null) {
			event.cancel();
			return;
		}
		game.invokeAsync(new Runnable() {

			@Override
			public void run() {

				TextInputDialog dialog = new TextInputDialog(initVal);
				dialog.setTitle(label);
				dialog.setHeaderText(label);
				dialog.setContentText(label);
				Optional<String> result = dialog.showAndWait();
				if (result.isPresent()) {
					event.input(result.get());
				} else {
					event.cancel();
				}

			}
		});
	}

	@Override
	public void sysDialog(final SysInput.ClickEvent event, String title, String text, String ok, String cancel) {
		if (game == null) {
			event.cancel();
			return;
		}
		game.invokeAsync(new Runnable() {

			@Override
			public void run() {

				Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle(title);
				alert.setHeaderText(text);
				alert.setContentText(text);

				ButtonType okBtn = new ButtonType(ok);
				ButtonType cancelBtn = new ButtonType(cancel);
				alert.getButtonTypes().setAll(okBtn, cancelBtn);

				Optional<ButtonType> result = alert.showAndWait();

				if (result.get() == okBtn) {
					event.clicked();
				} else {
					event.cancel();
				}
			}
		});

	}

	public JavaFXResizeCanvas getCanvas() {
		return fxCanvas;
	}

	@Override
	public LGame getGame() {
		return game;
	}

}
