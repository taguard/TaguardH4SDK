package com.moko.beaconxplus.dialog;

import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;

import com.moko.beaconxplus.R;
import com.moko.beaconxplus.utils.ToastUtils;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PasswordDialog extends MokoBaseDialog {

    public static final String TAG = PasswordDialog.class.getSimpleName();

    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";
    @Bind(R.id.et_password)
    EditText etPassword;

    private String password;

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_password;
    }

    @Override
    public void bindView(View v) {
        ButterKnife.bind(this, v);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        if (!TextUtils.isEmpty(password)) {
            etPassword.setText(password);
            etPassword.setSelection(password.length());
        }

    }

    @Override
    public int getDialogStyle() {
        return R.style.CenterDialog;
    }

    @Override
    public int getGravity() {
        return Gravity.CENTER;
    }

    @Override
    public String getFragmentTag() {
        return TAG;
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    @Override
    public boolean getCancelOutside() {
        return false;
    }

    @Override
    public boolean getCancellable() {
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    @OnClick({R.id.tv_password_cancel, R.id.tv_password_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_password_cancel:
                dismiss();
                if (passwordClickListener != null) {
                    passwordClickListener.onDismiss();
                }
                break;
            case R.id.tv_password_ensure:
                dismiss();
                if (TextUtils.isEmpty(etPassword.getText().toString())) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_null));
                    return;
                }
                if (passwordClickListener != null)
                    passwordClickListener.onEnsureClicked(etPassword.getText().toString());
                break;
        }
    }

    private PasswordClickListener passwordClickListener;

    public void setOnPasswordClicked(PasswordClickListener passwordClickListener) {
        this.passwordClickListener = passwordClickListener;
    }

    public interface PasswordClickListener {

        void onEnsureClicked(String password);

        void onDismiss();
    }

    public void setPassword(String password) {
        this.password = password;
    }
}