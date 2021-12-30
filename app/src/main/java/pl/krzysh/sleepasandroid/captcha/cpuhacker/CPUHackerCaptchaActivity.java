package pl.krzysh.sleepasandroid.captcha.cpuhacker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.urbandroid.sleep.captcha.CaptchaSupport;
import com.urbandroid.sleep.captcha.CaptchaSupportFactory;
import com.urbandroid.sleep.captcha.RemainingTimeListener;

public class CPUHackerCaptchaActivity extends Activity {

    private static final String TAG = "CPUHackerCaptcha";

    private CaptchaSupport captchaSupport;

    private CPUHackerChallenge challenge;

    private ViewGroup rootLayout;
    private TextView timeoutView;
    private ImageView iconArrowBefore;
    private ImageView iconArrowAfter;
    private TextView inputEAX;
    private TextView inputEBX;
    private TextView inputECX;
    private TextView inputEDX;
    private TextView inputInstruction;
    private EditText outputEAX;
    private EditText outputEBX;
    private EditText outputECX;
    private EditText outputEDX;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.main);

        captchaSupport = CaptchaSupportFactory
            .create(this)
            .setRemainingTimeListener(remainingTimeListener);

        rootLayout = (ViewGroup) findViewById(R.id.rootLayout);
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);

        timeoutView = (TextView) findViewById(R.id.timeout);
        timeoutView.setText("");
        iconArrowBefore = (ImageView) findViewById(R.id.icon_before_cpu);
        iconArrowAfter = (ImageView) findViewById(R.id.icon_after_cpu);

        inputEAX = (TextView) findViewById(R.id.text_input_eax);
        inputEBX = (TextView) findViewById(R.id.text_input_ebx);
        inputECX = (TextView) findViewById(R.id.text_input_ecx);
        inputEDX = (TextView) findViewById(R.id.text_input_edx);
        inputInstruction = (TextView) findViewById(R.id.text_instruction);
        outputEAX = (EditText) findViewById(R.id.text_output_eax);
        outputEBX = (EditText) findViewById(R.id.text_output_ebx);
        outputECX = (EditText) findViewById(R.id.text_output_ecx);
        outputEDX = (EditText) findViewById(R.id.text_output_edx);

        outputEAX.addTextChangedListener(textChangedListener);
        outputEBX.addTextChangedListener(textChangedListener);
        outputECX.addTextChangedListener(textChangedListener);
        outputEDX.addTextChangedListener(textChangedListener);

        if (savedInstanceState != null)
            challenge = savedInstanceState.getParcelable("challenge");
        if (challenge == null)
            challenge = CPUHackerChallenge.generate();
        loadChallenge();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        captchaSupport = CaptchaSupportFactory
            .create(this, intent)
            .setRemainingTimeListener(remainingTimeListener);

        challenge = CPUHackerChallenge.generate();
        loadChallenge();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("challenge", challenge);
    }

    private void loadChallenge() {
        for(int i = 0; i < CPUHackerChallenge.REGISTERS.length; ++i)
            Log.d(TAG, "Input " + CPUHackerChallenge.REGISTERS[i] + " = " + String.format("%08X", challenge.inputs[i]));
        Log.d(TAG, "Instruction = " + challenge.instruction);
        for(int i = 0; i < CPUHackerChallenge.REGISTERS.length; ++i)
            Log.d(TAG, "Output " + CPUHackerChallenge.REGISTERS[i] + " = " + String.format("%08X", challenge.outputs[i]));

        inputEAX.setText(String.format("%08X", challenge.inputs[0]));
        inputEBX.setText(String.format("%08X", challenge.inputs[1]));
        inputECX.setText(String.format("%08X", challenge.inputs[2]));
        inputEDX.setText(String.format("%08X", challenge.inputs[3]));
        inputInstruction.setText(challenge.instruction);
        outputEAX.setText("");
        outputEBX.setText("");
        outputECX.setText("");
        outputEDX.setText("");
        outputEAX.requestFocus();
    }

    private void checkSolution() {
        String eax = outputEAX.getText().toString();
        String ebx = outputEBX.getText().toString();
        String ecx = outputECX.getText().toString();
        String edx = outputEDX.getText().toString();

        if (eax.length() == 8 && ebx.length() == 8 && ecx.length() == 8 && edx.length() == 8) {
            Log.d(TAG, "EAX = " + eax);
            Log.d(TAG, "EBX = " + ebx);
            Log.d(TAG, "ECX = " + ecx);
            Log.d(TAG, "EDX = " + edx);

            if (eax.equals(String.format("%08X", challenge.outputs[0])) &&
                ebx.equals(String.format("%08X", challenge.outputs[1])) &&
                ecx.equals(String.format("%08X", challenge.outputs[2])) &&
                edx.equals(String.format("%08X", challenge.outputs[3]))) {
                Log.d(TAG, "Captcha solved!");
                captchaSupport.solved();
                finish();
            } else {
                challenge = CPUHackerChallenge.generate();
                loadChallenge();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        captchaSupport.unsolved();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        captchaSupport.alive();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        captchaSupport.destroy();
    }

    private final RemainingTimeListener remainingTimeListener = (seconds, aliveTimeout) -> {
        Log.d(TAG, "Remaining time: " + seconds + "/" + aliveTimeout);
        timeoutView.setText(String.valueOf(seconds));
    };

    private final TextWatcher textChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkSolution();
        }
    };

    private final ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            if (iconArrowBefore == null || iconArrowAfter == null)
                return;

            int heightDiff = rootLayout.getRootView().getHeight() - rootLayout.getHeight();
            int contentViewTop = getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();

            if (heightDiff <= contentViewTop) {
                iconArrowBefore.setVisibility(View.VISIBLE);
                iconArrowAfter.setVisibility(View.VISIBLE);
            } else {
                iconArrowBefore.setVisibility(View.GONE);
                iconArrowAfter.setVisibility(View.GONE);
            }
        }
    };
}