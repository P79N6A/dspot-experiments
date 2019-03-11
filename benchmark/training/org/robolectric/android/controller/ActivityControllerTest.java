package org.robolectric.android.controller;


import Build.VERSION_CODES;
import Configuration.ORIENTATION_PORTRAIT;
import Intent.ACTION_VIEW;
import Window.FEATURE_ACTION_BAR;
import android.R.id.content;
import android.R.style.Theme_Black;
import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.view.ViewRootImpl;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.Scheduler;
import org.robolectric.util.TestRunnable;

import static org.robolectric.R.style.Theme_Robolectric;


@RunWith(AndroidJUnit4.class)
public class ActivityControllerTest {
    private static final List<String> transcript = new ArrayList<>();

    private final ComponentName componentName = new ComponentName("org.robolectric", ActivityControllerTest.MyActivity.class.getName());

    private final ActivityController<ActivityControllerTest.MyActivity> controller = Robolectric.buildActivity(ActivityControllerTest.MyActivity.class);

    @Test
    public void canCreateActivityNotListedInManifest() {
        Activity activity = Robolectric.setupActivity(Activity.class);
        assertThat(activity).isNotNull();
        assertThat(activity.getThemeResId()).isEqualTo(Theme_Robolectric);
    }

    public static class TestDelayedPostActivity extends Activity {
        TestRunnable r1 = new TestRunnable();

        TestRunnable r2 = new TestRunnable();

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Handler h = new Handler();
            h.post(r1);
            h.postDelayed(r2, 60000);
        }
    }

    @Test
    public void pendingTasks_areRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
        final Scheduler s = Robolectric.getForegroundThreadScheduler();
        final long startTime = s.getCurrentTime();
        ActivityControllerTest.TestDelayedPostActivity activity = Robolectric.setupActivity(ActivityControllerTest.TestDelayedPostActivity.class);
        assertThat(activity.r1.wasRun).named("immediate task").isTrue();
        assertThat(s.getCurrentTime()).named("currentTime").isEqualTo(startTime);
    }

    @Test
    public void delayedTasks_areNotRunEagerly_whenActivityIsStarted_andSchedulerUnPaused() {
        // Regression test for issue #1509
        final Scheduler s = Robolectric.getForegroundThreadScheduler();
        final long startTime = s.getCurrentTime();
        ActivityControllerTest.TestDelayedPostActivity activity = Robolectric.setupActivity(ActivityControllerTest.TestDelayedPostActivity.class);
        assertThat(activity.r2.wasRun).named("before flush").isFalse();
        assertThat(s.getCurrentTime()).named("currentTime before flush").isEqualTo(startTime);
        s.advanceToLastPostedRunnable();
        assertThat(activity.r2.wasRun).named("after flush").isTrue();
        assertThat(s.getCurrentTime()).named("currentTime after flush").isEqualTo((startTime + 60000));
    }

    @Test
    public void shouldSetIntent() throws Exception {
        ActivityControllerTest.MyActivity myActivity = controller.create().get();
        assertThat(getIntent()).isNotNull();
        assertThat(getIntent().getComponent()).isEqualTo(componentName);
    }

    @Test
    public void shouldSetIntentComponentWithCustomIntentWithoutComponentSet() throws Exception {
        ActivityControllerTest.MyActivity myActivity = Robolectric.buildActivity(ActivityControllerTest.MyActivity.class, new Intent(Intent.ACTION_VIEW)).create().get();
        assertThat(getIntent().getAction()).isEqualTo(ACTION_VIEW);
        assertThat(getIntent().getComponent()).isEqualTo(componentName);
    }

    @Test
    public void shouldSetIntentForGivenActivityInstance() throws Exception {
        ActivityController<ActivityControllerTest.MyActivity> activityController = ActivityController.of(new ActivityControllerTest.MyActivity()).create();
        assertThat(getIntent()).isNotNull();
    }

    @Test
    public void whenLooperIsNotPaused_shouldCreateWithMainLooperPaused() throws Exception {
        ShadowLooper.unPauseMainLooper();
        controller.create();
        assertThat(Shadows.shadowOf(Looper.getMainLooper()).isPaused()).isFalse();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnCreate", "onCreate");
    }

    @Test
    public void whenLooperIsAlreadyPaused_shouldCreateWithMainLooperPaused() throws Exception {
        ShadowLooper.pauseMainLooper();
        controller.create();
        assertThat(Shadows.shadowOf(Looper.getMainLooper()).isPaused()).isTrue();
        assertThat(ActivityControllerTest.transcript).contains("finishedOnCreate");
        ShadowLooper.unPauseMainLooper();
        assertThat(ActivityControllerTest.transcript).contains("onCreate");
    }

    @Test
    public void visible_addsTheDecorViewToTheWindowManager() {
        controller.create().visible();
        assertThat(getWindow().getDecorView().getParent().getClass()).isEqualTo(ViewRootImpl.class);
    }

    @Test
    public void start_callsPerformStartWhilePaused() {
        controller.create().start();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnStart", "onStart");
    }

    @Test
    public void stop_callsPerformStopWhilePaused() {
        controller.create().start().stop();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnStop", "onStop");
    }

    @Test
    public void restart_callsPerformRestartWhilePaused() {
        controller.create().start().stop().restart();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnRestart", "onRestart");
    }

    @Test
    public void pause_callsPerformPauseWhilePaused() {
        controller.create().pause();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnPause", "onPause");
    }

    @Test
    public void resume_callsPerformResumeWhilePaused() {
        controller.create().start().resume();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnResume", "onResume");
    }

    @Test
    public void destroy_callsPerformDestroyWhilePaused() {
        controller.create().destroy();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnDestroy", "onDestroy");
    }

    @Test
    public void postCreate_callsOnPostCreateWhilePaused() {
        controller.create().postCreate(new Bundle());
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnPostCreate", "onPostCreate");
    }

    @Test
    public void postResume_callsOnPostResumeWhilePaused() {
        controller.create().postResume();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnPostResume", "onPostResume");
    }

    @Test
    public void restoreInstanceState_callsPerformRestoreInstanceStateWhilePaused() {
        controller.create().restoreInstanceState(new Bundle());
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnRestoreInstanceState", "onRestoreInstanceState");
    }

    @Test
    public void newIntent_callsOnNewIntentWhilePaused() {
        controller.create().newIntent(new Intent(Intent.ACTION_VIEW));
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnNewIntent", "onNewIntent");
    }

    @Test
    public void userLeaving_callsPerformUserLeavingWhilePaused() {
        controller.create().userLeaving();
        assertThat(ActivityControllerTest.transcript).containsAllOf("finishedOnUserLeaveHint", "onUserLeaveHint");
    }

    @Test
    public void setup_callsLifecycleMethodsAndMakesVisible() {
        controller.setup();
        assertThat(ActivityControllerTest.transcript).containsAllOf("onCreate", "onStart", "onPostCreate", "onResume", "onPostResume");
        assertThat(getWindow().getDecorView().getParent().getClass()).isEqualTo(ViewRootImpl.class);
    }

    @Test
    public void setupWithBundle_callsLifecycleMethodsAndMakesVisible() {
        controller.setup(new Bundle());
        assertThat(ActivityControllerTest.transcript).containsAllOf("onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume", "onPostResume");
        assertThat(getWindow().getDecorView().getParent().getClass()).isEqualTo(ViewRootImpl.class);
    }

    @Test
    @Config(sdk = VERSION_CODES.KITKAT)
    public void attach_shouldWorkWithAPI19() {
        ActivityControllerTest.MyActivity activity = Robolectric.buildActivity(ActivityControllerTest.MyActivity.class).create().get();
        assertThat(activity).isNotNull();
    }

    @Test
    public void configurationChange_callsLifecycleMethodsAndAppliesConfig() {
        Configuration config = new Configuration(ApplicationProvider.getApplicationContext().getResources().getConfiguration());
        final float newFontScale = config.fontScale *= 2;
        controller.setup();
        ActivityControllerTest.transcript.clear();
        controller.configurationChange(config);
        assertThat(ActivityControllerTest.transcript).containsAllOf("onPause", "onStop", "onDestroy", "onCreate", "onStart", "onRestoreInstanceState", "onPostCreate", "onResume", "onPostResume");
        assertThat(controller.get().getResources().getConfiguration().fontScale).isEqualTo(newFontScale);
    }

    @Test
    public void configurationChange_callsOnConfigurationChangedAndAppliesConfigWhenAllManaged() {
        Configuration config = new Configuration(ApplicationProvider.getApplicationContext().getResources().getConfiguration());
        final float newFontScale = config.fontScale *= 2;
        ActivityController<ActivityControllerTest.ConfigAwareActivity> configController = Robolectric.buildActivity(ActivityControllerTest.ConfigAwareActivity.class).setup();
        ActivityControllerTest.transcript.clear();
        configController.configurationChange(config);
        assertThat(ActivityControllerTest.transcript).contains("onConfigurationChanged");
        assertThat(configController.get().getResources().getConfiguration().fontScale).isEqualTo(newFontScale);
    }

    @Test
    public void configurationChange_callsLifecycleMethodsAndAppliesConfigWhenAnyNonManaged() {
        Configuration config = new Configuration(ApplicationProvider.getApplicationContext().getResources().getConfiguration());
        final float newFontScale = config.fontScale *= 2;
        final int newOrientation = config.orientation = ((config.orientation) + 1) % 3;
        ActivityController<ActivityControllerTest.ConfigAwareActivity> configController = Robolectric.buildActivity(ActivityControllerTest.ConfigAwareActivity.class).setup();
        ActivityControllerTest.transcript.clear();
        configController.configurationChange(config);
        assertThat(ActivityControllerTest.transcript).containsAllOf("onPause", "onStop", "onDestroy", "onCreate", "onStart", "onResume");
        assertThat(configController.get().getResources().getConfiguration().fontScale).isEqualTo(newFontScale);
        assertThat(configController.get().getResources().getConfiguration().orientation).isEqualTo(newOrientation);
    }

    @Test
    @Config(qualifiers = "land")
    public void noArgsConfigurationChange_appliesChangedSystemConfiguration() throws Exception {
        ActivityController<ActivityControllerTest.ConfigAwareActivity> configController = Robolectric.buildActivity(ActivityControllerTest.ConfigAwareActivity.class).setup();
        RuntimeEnvironment.setQualifiers("port");
        configController.configurationChange();
        assertThat(configController.get().newConfig.orientation).isEqualTo(ORIENTATION_PORTRAIT);
    }

    @Test
    @Config(qualifiers = "land")
    public void configurationChange_restoresTheme() {
        Configuration config = new Configuration(ApplicationProvider.getApplicationContext().getResources().getConfiguration());
        config.orientation = Configuration.ORIENTATION_PORTRAIT;
        controller.get().setTheme(Theme_Black);
        controller.setup();
        ActivityControllerTest.transcript.clear();
        controller.configurationChange(config);
        int restoredTheme = Shadows.shadowOf(((ContextThemeWrapper) (controller.get()))).callGetThemeResId();
        assertThat(restoredTheme).isEqualTo(Theme_Black);
    }

    @Test
    @Config(qualifiers = "land")
    public void configurationChange_reattachesRetainedFragments() {
        Configuration config = new Configuration(ApplicationProvider.getApplicationContext().getResources().getConfiguration());
        config.orientation = Configuration.ORIENTATION_PORTRAIT;
        ActivityController<ActivityControllerTest.NonConfigStateActivity> configController = Robolectric.buildActivity(ActivityControllerTest.NonConfigStateActivity.class).setup();
        ActivityControllerTest.NonConfigStateActivity activity = configController.get();
        Fragment retainedFragment = activity.retainedFragment;
        Fragment otherFragment = activity.nonRetainedFragment;
        configController.configurationChange(config);
        activity = configController.get();
        assertThat(activity.retainedFragment).isNotNull();
        assertThat(activity.retainedFragment).isSameAs(retainedFragment);
        assertThat(activity.nonRetainedFragment).isNotNull();
        assertThat(activity.nonRetainedFragment).isNotSameAs(otherFragment);
    }

    @Test
    public void windowFocusChanged() {
        controller.setup();
        assertThat(ActivityControllerTest.transcript).doesNotContain("finishedOnWindowFocusChanged");
        assertThat(controller.get().hasWindowFocus()).isFalse();
        ActivityControllerTest.transcript.clear();
        controller.windowFocusChanged(true);
        assertThat(ActivityControllerTest.transcript).containsExactly("finishedOnWindowFocusChanged");
        assertThat(controller.get().hasWindowFocus()).isTrue();
    }

    public static class MyActivity extends Activity {
        @Override
        protected void onRestoreInstanceState(Bundle savedInstanceState) {
            super.onRestoreInstanceState(savedInstanceState);
            transcribeWhilePaused("onRestoreInstanceState");
            ActivityControllerTest.transcript.add("finishedOnRestoreInstanceState");
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getWindow().requestFeature(FEATURE_ACTION_BAR);
            setContentView(new android.widget.LinearLayout(ApplicationProvider.getApplicationContext()));
            transcribeWhilePaused("onCreate");
            ActivityControllerTest.transcript.add("finishedOnCreate");
        }

        @Override
        protected void onPostCreate(Bundle savedInstanceState) {
            super.onPostCreate(savedInstanceState);
            transcribeWhilePaused("onPostCreate");
            ActivityControllerTest.transcript.add("finishedOnPostCreate");
        }

        @Override
        protected void onPostResume() {
            super.onPostResume();
            transcribeWhilePaused("onPostResume");
            ActivityControllerTest.transcript.add("finishedOnPostResume");
        }

        @Override
        protected void onDestroy() {
            super.onDestroy();
            transcribeWhilePaused("onDestroy");
            ActivityControllerTest.transcript.add("finishedOnDestroy");
        }

        @Override
        protected void onStart() {
            super.onStart();
            transcribeWhilePaused("onStart");
            ActivityControllerTest.transcript.add("finishedOnStart");
        }

        @Override
        protected void onStop() {
            super.onStop();
            transcribeWhilePaused("onStop");
            ActivityControllerTest.transcript.add("finishedOnStop");
        }

        @Override
        protected void onResume() {
            super.onResume();
            transcribeWhilePaused("onResume");
            ActivityControllerTest.transcript.add("finishedOnResume");
        }

        @Override
        protected void onRestart() {
            super.onRestart();
            transcribeWhilePaused("onRestart");
            ActivityControllerTest.transcript.add("finishedOnRestart");
        }

        @Override
        protected void onPause() {
            super.onPause();
            transcribeWhilePaused("onPause");
            ActivityControllerTest.transcript.add("finishedOnPause");
        }

        @Override
        protected void onNewIntent(Intent intent) {
            super.onNewIntent(intent);
            transcribeWhilePaused("onNewIntent");
            ActivityControllerTest.transcript.add("finishedOnNewIntent");
        }

        @Override
        protected void onUserLeaveHint() {
            super.onUserLeaveHint();
            transcribeWhilePaused("onUserLeaveHint");
            ActivityControllerTest.transcript.add("finishedOnUserLeaveHint");
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            transcribeWhilePaused("onConfigurationChanged");
            ActivityControllerTest.transcript.add("finishedOnConfigurationChanged");
        }

        @Override
        public void onWindowFocusChanged(boolean newFocus) {
            super.onWindowFocusChanged(newFocus);
            ActivityControllerTest.transcript.add("finishedOnWindowFocusChanged");
        }

        private void transcribeWhilePaused(final String event) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ActivityControllerTest.transcript.add(event);
                }
            });
        }
    }

    public static class ConfigAwareActivity extends ActivityControllerTest.MyActivity {
        Configuration newConfig;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState != null) {
                assertThat(savedInstanceState.getSerializable("test")).isNotNull();
            }
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putSerializable("test", new Exception());
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            this.newConfig = new Configuration(newConfig);
            super.onConfigurationChanged(newConfig);
        }
    }

    public static final class NonConfigStateActivity extends Activity {
        Fragment retainedFragment;

        Fragment nonRetainedFragment;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (savedInstanceState == null) {
                retainedFragment = new Fragment();
                retainedFragment.setRetainInstance(true);
                nonRetainedFragment = new Fragment();
                getFragmentManager().beginTransaction().add(content, retainedFragment, "retained").add(content, nonRetainedFragment, "non-retained").commit();
            } else {
                retainedFragment = getFragmentManager().findFragmentByTag("retained");
                nonRetainedFragment = getFragmentManager().findFragmentByTag("non-retained");
            }
        }
    }
}
