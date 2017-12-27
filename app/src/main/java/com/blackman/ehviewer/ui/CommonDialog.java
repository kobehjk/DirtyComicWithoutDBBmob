package com.blackman.ehviewer.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.blackman.ehviewer.EhApplication;
import com.blackman.ehviewer.GScreenAdapter;
import com.blackman.ehviewer.R;
import com.hippo.yorozuya.StringUtils;

/**
 * Created by hejinkun on 2017/12/20.
 */

public class CommonDialog extends Dialog{

    public enum DialogType{TEXT ,WEBVIEW,INPUT}
    public enum ButtonType{BTN_OKCANCEL,BTN_CANCEL}

    LinearLayout mContentLayout;

    TextView mTitleTx;
    Button mLeftBtn,mRightBtn;

    Context mContext;

    DialogType mDialogType = DialogType.TEXT;
    ButtonType mBtnType = ButtonType.BTN_OKCANCEL;

    TextView mContentText;
    EditText mContentEdit;
//    HtmlView  mContentWeb;


    public CommonDialog(Context context ) {
        super(context, R.style.DialogStyle);
        mContext = context;

    }

    public CommonDialog(Context context  , DialogType dialogType) {
        super(context, R.style.DialogStyle);

        mDialogType = dialogType;
        mContext = context;
    }

    public CommonDialog bulider() {
        LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentView= li.inflate(R.layout.dlg_commom ,null);
        mContentLayout = (LinearLayout)contentView.findViewById(R.id.content);
        mTitleTx = (TextView) contentView.findViewById(R.id.title);
        mLeftBtn = (Button) contentView.findViewById(R.id.btn_left);
        mRightBtn = (Button) contentView.findViewById(R.id.btn_right);
        ImageView closeBtn = (ImageView)contentView.findViewById(R.id.dialog_close);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });


        //默认右边按钮为取消
        mRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        buildContentView();

        setContentView(contentView);
        setCanceledOnTouchOutside(false);

        Window dialogWindow = getWindow();
        WindowManager m = ((Activity)mContext).getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = dialogWindow.getAttributes(); // 获取对话框当前的参数值
        p.width = (int) (d.getWidth()- GScreenAdapter.instance().dip2px(10));
        getWindow().setAttributes(p);

        return this;
    }

    private void buildContentView() {
        if(mDialogType == DialogType.TEXT){
            mContentText = new TextView(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.gravity = Gravity.CENTER;
            lp.rightMargin =GScreenAdapter.instance().dip2px(10);
            lp.leftMargin =GScreenAdapter.instance().dip2px(10);
            mContentText.setTextSize(16);
            mContentText.setTextColor(Color.BLACK);
            mContentLayout.addView(mContentText , lp);
        }
//        if(mDialogType == DialogType.WEBVIEW){
//            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT,
//                    GScreenAdapter.instance().dip2px(220)
//            );
//            mContentWeb = new HtmlView(mContext);
//            mContentLayout.addView(mContentWeb , lp);
//        }
        if(mDialogType == DialogType.INPUT){

            mContentEdit = new EditText(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            lp.setMargins(20,5,20,0);
            mContentLayout.addView(mContentEdit , lp);
        }
    }

    public CommonDialog setButtonType(ButtonType type){
        if(mBtnType == ButtonType.BTN_CANCEL&&mLeftBtn!=null){
            mLeftBtn.setVisibility(View.INVISIBLE);
        }
        return  this;
    }

//    public CommonDialog setUrl( String url){
//        if(!StringUtils.equals(url,"")&&mContentWeb!=null){
//            url = url + "?time="+String.valueOf (new Date().getTime());
//            mContentWeb.load(url);
//
//        }else{
//            setContentViewShow(false);
//        }
//        return this;
//    }

    public CommonDialog setMessage(String msg){
        if(!StringUtils.equals(msg,"")&&mContentText!=null){
            mContentText.setText(msg);
        }
        return this;
    }
    public CommonDialog setTitle(String title){
        if(!StringUtils.equals(title,"")&&mTitleTx!=null){
            mTitleTx.setText(title);
        }
        return this;
    }
    public CommonDialog setLeftButtonText(String label){
        if(!StringUtils.equals(label,"")&&mLeftBtn!=null)
            mLeftBtn.setText(label);
        return this;
    }
    public CommonDialog setRightButtonText(String label){
        if(!StringUtils.equals(label,"")&&mRightBtn!=null)
            mRightBtn.setText(label);
        return this;
    }

    public CommonDialog setLeftButtonClickListener(View.OnClickListener listener){
        if(listener!=null&&mLeftBtn!=null){
            mLeftBtn.setOnClickListener(listener);
        }
        return this;
    }
    public CommonDialog setRightButtonClickListener(View.OnClickListener listener){
        if(listener!=null&&mRightBtn!=null){
            mRightBtn.setOnClickListener(listener);
        }
        return this;
    }

    public String getInputText(){
        if(mContentLayout!=null)
            return  mContentEdit.getText().toString();
        return "";
    }

    public void setContentViewShow(boolean isShow){
        mContentLayout.setVisibility(isShow ? View.VISIBLE :View.GONE);
    }

    public void editClose(){
        InputMethodManager imm = (InputMethodManager) EhApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mContentEdit.getWindowToken(), 0);
        dismiss();

    }
}
