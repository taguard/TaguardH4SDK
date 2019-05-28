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

public class ModifyPasswordDialog extends MokoBaseDialog {
    public static final String TAG = ModifyPasswordDialog.class.getSimpleName();


    @Bind(R.id.et_new_password)
    EditText etNewPassword;
    @Bind(R.id.et_new_password_re)
    EditText etNewPasswordRe;
    private final String FILTER_ASCII = "\\A\\p{ASCII}*\\z";

    @Override
    public int getLayoutRes() {
        return R.layout.dialog_change_password;
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
        etNewPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
        etNewPasswordRe.setFilters(new InputFilter[]{new InputFilter.LengthFilter(16), filter});
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

    @OnClick({R.id.tv_cancel, R.id.tv_ensure})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_cancel:
                dismiss();
                break;
            case R.id.tv_ensure:
                String newPassword = etNewPassword.getText().toString();
                String newPasswordRe = etNewPasswordRe.getText().toString();
                if (TextUtils.isEmpty(newPassword)) {
                    ToastUtils.showToast(getContext(), getContext().getString(R.string.password_length));
                    return;
                }
                if (TextUtils.isEmpty(newPasswordRe)) {
                    ToastUtils.showToast(getContext(), "The two passwords differ.");
                    return;
                }
                if (!newPasswordRe.equals(newPassword)) {
                    ToastUtils.showToast(getContext(), "The two passwords differ.");
                    return;
                }
                dismiss();
                if (modifyPasswordClickListener != null)
                    modifyPasswordClickListener.onEnsureClicked(etNewPassword.getText().toString());
                break;
        }
    }

    private ModifyPasswordClickListener modifyPasswordClickListener;

    public void setOnModifyPasswordClicked(ModifyPasswordClickListener modifyPasswordClickListener) {
        this.modifyPasswordClickListener = modifyPasswordClickListener;
    }

    public interface ModifyPasswordClickListener {

        void onEnsureClicked(String password);
    }
}
