package com.qicheng.business.ui;


import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.qicheng.R;
import com.qicheng.business.cache.Cache;
import com.qicheng.business.logic.LogicFactory;
import com.qicheng.business.logic.TripLogic;
import com.qicheng.business.logic.event.TripEventArgs;
import com.qicheng.business.module.TrainStation;
import com.qicheng.business.module.Trip;
import com.qicheng.business.protocol.GetTrainInfoProcess;
import com.qicheng.framework.event.EventArgs;
import com.qicheng.framework.event.EventId;
import com.qicheng.framework.event.EventListener;
import com.qicheng.framework.event.OperErrorCode;
import com.qicheng.framework.event.UIEventListener;
import com.qicheng.framework.ui.base.BaseFragment;
import com.qicheng.framework.ui.helper.Alert;
import com.qicheng.framework.util.StringUtil;
import com.qicheng.util.Const;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class TrainPickFragment extends BaseFragment  implements Serializable{
    /* 请求码 */
    private static final int DATE_REQUEST_CODE = 0;
    private static final int TRIP_REQUEST_CODE = 1;

    private EditText mTripDate;
    private AutoCompleteTextView mTrainCode;
    private Button mNextButton;

    private StringBuffer paramTripDate = new StringBuffer();


    public TrainPickFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        ActionBar bar = getActivity().getActionBar();
        if(bar!=null){
            bar.setTitle(getResources().getString(R.string.train_pick_activity_title));
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                getActivity().finish();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View convertView = inflater.inflate(R.layout.fragment_train_pick, container, false);
        mTrainCode = (AutoCompleteTextView)convertView.findViewById(R.id.editText_train_code);
        //自动完成TextView adapter
        String[] s = Cache.getInstance().getTrainList().toArray(new String[0]);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_dropdown_item_1line,s);
        mTrainCode.setAdapter(adapter);
        mTrainCode.addTextChangedListener( new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do noting
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                //验证是否为空，设置下一步按钮状态
                if(!StringUtil.isEmpty(s.toString())){
                    mNextButton.setEnabled(true);
                }else {
                    mNextButton.setEnabled(false);
                }
            }
        });
        //行程日期
        mTripDate = (EditText)convertView.findViewById(R.id.editText_date);
        updateTripDate(new Date());
        mTripDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //弹出日期选择框
                showDatePickDialog();
            }
        });
        //下一步按钮
        mNextButton = (Button)convertView.findViewById(R.id.button_next);
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TripLogic logic = (TripLogic)LogicFactory.self().get(LogicFactory.Type.Trip);
                logic.getTrainInfo(mTrainCode.getText().toString(), createUIEventListener(new EventListener() {
                    @Override
                    public void onEvent(EventId id, EventArgs args) {
                        stopLoading();
                        TripEventArgs result =  (TripEventArgs)args;
                        OperErrorCode errCode = result.getErrCode();
                        switch(errCode) {
                            case Success:
                                ArrayList<TrainStation> stations = result.getTrainStations();
                                Intent intent = new Intent();
                                intent.putExtra("trainCode",mTrainCode.getText().toString());
                                intent.putExtra("tripDate", paramTripDate.toString());
                                intent.putExtra("stationList", stations);
                                intent.setClass(getActivity(), AddTripActivity.class);
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intent, TRIP_REQUEST_CODE);
                                break;
                            default:
                                Alert.Toast(getResources().getString(R.string.no_such_train_err));
                                break;
                        }
                    }
                }));
                startLoading();
            }
        });
        return convertView;
    }

    /**
     * 显示生日选择对话框
     */
    private void showDatePickDialog() {
        FragmentManager fm = getActivity().getFragmentManager();
        DatePickFragment dialog = DatePickFragment.newInstance(null);
        dialog.setDialogTitle("请选择生日");
        dialog.setTargetFragment(this, DATE_REQUEST_CODE);
        dialog.show(fm, "date");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //结果码不等于取消时候
        if (resultCode != Activity.RESULT_CANCELED) {
            switch (requestCode) {
                case DATE_REQUEST_CODE:
                    Date date = (Date) data.getSerializableExtra(DatePickFragment.EXTRA_DATE);
                    updateTripDate(date);
                    break;
                case TRIP_REQUEST_CODE:
                    sendResult(Const.ResponseResultCode.RESULT_SUCCESS,data);
                    getActivity().finish();
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 设置行程时间值
     * @param date
     */
    private void updateTripDate(Date date) {
        StringBuffer dateText = new StringBuffer();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        dateText.append(calendar.get(Calendar.YEAR));
        paramTripDate.append(calendar.get(Calendar.YEAR));
        dateText.append("年");
        dateText.append(calendar.get(Calendar.MONTH) + 1);
        if(Calendar.MONTH + 1<10){
            paramTripDate.append("0"+(calendar.get(Calendar.MONTH) + 1));
        }else {
            paramTripDate.append((calendar.get(Calendar.MONTH) + 1));
        }
        dateText.append("月");
        dateText.append(calendar.get(Calendar.DAY_OF_MONTH));
        paramTripDate.append(calendar.get(Calendar.DAY_OF_MONTH));
        dateText.append("日");
        mTripDate.setText(dateText.toString());
        if(!StringUtil.isEmpty(mTrainCode.getText().toString())){
            mNextButton.setEnabled(true);
        }

    }

    private void sendResult(int resultCode,Intent i) {
        getActivity().setResult(resultCode,i);
    }

}
