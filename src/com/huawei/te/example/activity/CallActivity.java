package com.huawei.te.example.activity;

import com.huawei.common.CustomBroadcastConst;
import com.huawei.common.LogSDK;
import com.huawei.common.Resource;
import com.huawei.esdk.te.call.CallLogic;
import com.huawei.esdk.te.call.VideoHandler;
import com.huawei.esdk.te.data.Constants;
import com.huawei.esdk.te.data.Constants.CallConstant;
import com.huawei.esdk.te.data.Constants.MSG_FOR_HOMEACTIVITY;
import com.huawei.esdk.te.data.Constants.MsgCallFragment;
import com.huawei.manager.DataManager;
import com.huawei.te.example.App;
import com.huawei.te.example.CallControl;
import com.huawei.te.example.R;
import com.huawei.te.example.ResponseErrorCodeHandler;
import com.huawei.utils.StringUtil;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CallActivity extends BaseActivity
{

	private static final String TAG = CallActivity.class.getSimpleName();

	private static Instance instance = new Instance();

	private RegReceiver regReceiver;

	private static class Instance
	{
		/**
		 * 主界面实例
		 */
		private CallActivity ins;

		@Override
		public String toString()
		{
			return "Instance [ins=" + ins + ']';
		}
	}

	private Handler handler;

	private Button audioCallBtn;
	private Button videoCallBtn;
	private Button exitBtn;
	private EditText callNumEt;

	/**
	 * 通话区域layout
	 */
	private LinearLayout callAreaLayout;
	private CallFragment callFragment;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "CallActivity onCreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		initComponent();
		initHandler();
		registerBroadcast();
		instance.ins = this;
	}

	public static CallActivity getInstance()
	{
		return instance.ins;
	}

	public CallFragment getCallFragment()
	{
		return callFragment;
	}

	private void registerBroadcast()
	{
		Log.d(TAG, "registerBroadcast enter.");
		// 注册界面刷新
		if (regReceiver == null)
		{
			IntentFilter iFilter = new IntentFilter();
			iFilter.addAction(CustomBroadcastConst.ACTION_CONNECT_TO_SERVER);
			iFilter.addAction(CustomBroadcastConst.ACTION_LOGIN_RESPONSE);
			iFilter.addAction(CustomBroadcastConst.ACTION_REFRESHLICENSEFAILED_NOTIFY);
			// 注册重登陆广播 211010
			regReceiver = new RegReceiver();
			registerReceiver(regReceiver, iFilter);
		}
		Log.d(TAG, "registerBroadcast leave.");
	}

	/**
	 * 方法名称：unRegister 作者：wujingdong 方法描述：注销刷新界面的广播 输入参数： 返回类型：void 备注：
	 */
	private void unRegister()
	{
		if (regReceiver != null)
		{
			unregisterReceiver(regReceiver);
			regReceiver = null;
		}
	}

	private void initComponent()
	{

		// 初始化通话fragment
		initCallFragment();

		callAreaLayout = (LinearLayout) findViewById(R.id.linear_local);

		// 呼叫号码编辑框
		callNumEt = (EditText) findViewById(R.id.et_call_number);
		callNumEt.setText("01058889");
		// 语音拨号按键
		audioCallBtn = (Button) findViewById(R.id.btn_audio_call);
		// 视频拨号按键
		videoCallBtn = (Button) findViewById(R.id.btn_video_call);
		// 注销Button
		exitBtn = (Button) findViewById(R.id.btn_logout);

		audioCallBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Log.e(TAG, "for test voipstatus:" +
				// CallLogic.getIns().getVoipStatus());
				if (CallLogic.STATUS_CLOSE == CallLogic.getIns().getVoipStatus())
				{
					String callNumber = callNumEt.getText().toString();
					if (null == callNumber || 0 == callNumber.length())
					{
						return;
					}
					callFragment.sendHandlerMessage(MsgCallFragment.MSG_SHOW_AUDIOVIEW, callNumber);
				} else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
					builder.setTitle("提示");
					builder.setMessage("当前不能发起新呼叫");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});
		videoCallBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// Log.e(TAG, "for test voipstatus:" +
				// CallLogic.getIns().getVoipStatus());
				if (CallLogic.STATUS_CLOSE == CallLogic.getIns().getVoipStatus())
				{
					String callNumber = callNumEt.getText().toString();
					if (null == callNumber || 0 == callNumber.length())
					{
						return;
					}
					callFragment.sendHandlerMessage(MsgCallFragment.MSG_SHOW_VIDEOVIEW, callNumber);
				} else
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
					builder.setTitle("提示");
					builder.setMessage("当前不能发起新呼叫");
					builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.dismiss();
						}
					});
					builder.create().show();
				}
			}
		});

		exitBtn.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				logoutApp();
			}
		});
	}

	/**
	 * 初始化呼叫界面
	 */
	private void initCallFragment()
	{
		if (null != callFragment)
		{
			return;
		}
		// 获取fragment对象
		callFragment = new CallFragment();

		// 获取manager
		FragmentManager manager = this.getFragmentManager();
		FragmentTransaction transation = manager.beginTransaction();
		transation.replace(R.id.call_frag_layout, callFragment);
		try
		{
			transation.commitAllowingStateLoss();
		} catch (IllegalStateException e)
		{
			Log.d(TAG, "IllegalStateException error.");
		}
	}

	private void initHandler()
	{

		handler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				Log.d(TAG, "what:" + msg.what);
				parallelHandleMessageOne(msg);
				parallelHandleMessageTwo(msg);
				// parallelHandleMessageThree(msg);
				parallelHandleMessageFour(msg);
				// parallelHandleMessageFive(msg);
				parallelHandleMessageSix(msg);
				// parallelHandleMessageConf(msg);
				super.handleMessage(msg);
			}
		};
	}

	private void parallelHandleMessageOne(Message msg)
	{
		switch (msg.what) {
		case MSG_FOR_HOMEACTIVITY.MSG_LOGOUT_AND_REGISTE:
			// setSelfStatus(SelfSettingWindow.AWAY);
			// mServiceProxy.getCallManager().unRegister();
			// EspaceApp.getIns().setOnlineStatus(Constant.STATUS_OFFLINE);
			// doLogin();
			break;
		case Constants.MSG_SELF_CHANGE_STATE:
			// if (null == msg.obj)
			// {
			// return;
			// }
			// // 只有通话抛此消息，建立通话是busy，结束通话：恢复到上次设置的状态
			// setSelfFieldStatus((Byte) msg.obj == PersonalContact.BUSY ?
			// PersonalContact.BUSY :
			// ConfigAccount.getIns().getLoginAccount().getStatus());
			break;
		// begin added by pwx178217 reason：点击打开关于页签，锁屏后解锁关于页面关闭
		// 呼叫对端超时 关于界面关闭
		case Constants.DISMISS_ABOUT:
			// if (null == homeAboutLayout)
			// {
			// break;
			// }
			// showAbout = false;
			// homeAboutLayout.setVisibility(View.GONE);
			// if (null != shadView)
			// {
			// shadView.setVisibility(View.GONE);
			// }
			// clearTmpLicenceHtml();
			break;
		// end added by pwx178217 reason：点击打开关于页签，锁屏后解锁关于页面关闭
		case MSG_FOR_HOMEACTIVITY.BROADCAST_EVENT:
			Intent intent = (Intent) msg.obj;
			handlerBroadcastEvent(intent);
			break;
		// begin added by pwx178217 2013/8/24 reason：添加全屏 退出全屏操作
		case Constants.MSG_FULL_SCREEN:
			// showFullScreen();
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageTwo(Message msg)
	{
		switch (msg.what) {
		case Constants.MSG_PART_SCREEN:
			// exitFullScreen();
			// // 取消全屏时显示联系人界面
			// // showContactsFragment();
			break;
		case Constants.MSG_CALL_CLOSE_BACK_TO_HOME:
			// 收到挂断消息
			closeCallBackToHome();
			break;

		// 视频按钮可用
		case Constants.MSG_ENABLE_PREVIEWBTN:
			// setPreviewBtnUserable();
			break;

		case Constants.MSG_SHOW_CHATVIEW:
			// LogUI.d("[UC_UI] MSG_SHOW_CHATVIEW");
			// showChatView(msg);
			break;
		case Constants.MSG_INCOMING_INVITE:
			// // 防止界面还存在
			// removeCallComingActivity();
			// showCallInComingActivity((Intent) msg.obj);
			// ConfigApp.getInstance().setDestoryedCallActivity(false);
			// // isDestoryedCallActivity = false;
			break;
		case Constants.ADCONFIRMATION:
			// if (!(msg.obj instanceof Boolean)) {
			// LogUI.e("msg.obj not instanceof Boolean");
			// return;
			// }
			// if (null != settingFragment) {
			// settingFragment.setPassItemVisible((Boolean) msg.obj);
			// }
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageFour(Message msg)
	{
		switch (msg.what) {
		case MSG_FOR_HOMEACTIVITY.SET_SELF_SYSTEM_SETTING:
			// startSettingActivity(-1);
			break;
		case MSG_FOR_HOMEACTIVITY.SET_SELF_DISMISS:
			// dismissShadView();
			// end modified by pwx178217 2013/8/15 reason：取消遮罩层
			break;
		case Constants.MSG_INIT_VIDEO:
			VideoHandler.getIns().initCallVideo(CallActivity.this);
			break;
		case Constants.MSG_UNINIT_VIDEO:
			Log.d(TAG, "Enter MSG_UNINIT_VIDEO");
			VideoHandler.getIns().clearCallVideo();
			break;
		case Constants.CONTACT_EXPORT_OPERATE:
			// contactExport(msg.obj.toString(), fileTitleString);
			break;
		case Constants.CONTACT_IMPORT_OPERATE:
			// contactImport(msg.obj.toString(), fileTitleString);
			break;
		// 以后都由此转处理界面类的通知
		case CallConstant.VOIP_UPDATE_SINGLE:
			// 通知界面网络信号变更
			// VoiceQualityLevel level = (VoiceQualityLevel) msg.obj;
			// callFragment.updateSignal(level);
			break;
		case Constants.MSG_NOTIFY_FRAMESIZE_RESET:
			// callFragment.reloadLocalHideView();
			break;
		// end add by l00208218 9.04 通知重新设置分辨率后刷新视频窗口
		case CallConstant.VOIP_CALL_HANG_UP:
			// 关闭弹出对话框
			// HomeActivity.this.dismissAlertDialog();
			Toast.makeText(CallActivity.this, ((String) msg.obj), Toast.LENGTH_LONG).show();
			break;
		case Constants.CONTACT_DOC_SHARE:
			// ActivityStackManager.INSTANCE.getImgFileListActivityAndRemove();
			// showPdfview(msg.obj);
			break;
		default:
			break;
		}
	}

	private void parallelHandleMessageSix(Message msg)
	{
		switch (msg.what) {
		// 将呼叫的逻辑移到callfragment里
		case CallConstant.SHOW_CALL_LAYOUT:
			// 显示呼出界面
			showCallLayout();
			break;
		case CallConstant.CLOSE_CAMERA:
			// 关闭本地视频预览
			closeLocalCamera();
			break;
		// 将呼叫的逻辑移到callfragment里
		case Constants.BACK_TO_MAIN_VIEW:
			// if (!ConfigApp.getInstance().isUsePadLayout()) {
			// showScreenWithTitle();
			// }
			// CallLogic.getIns().setMainView(true);
			// if (homeLeft.isShown()) {
			// homeTipButton.setText((String) msg.obj);
			// return;
			// }
			// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
			// homeLeft.setVisibility(View.VISIBLE);
			// callAreaLayout.setVisibility(View.GONE);
			// homeTipButton.setVisibility(View.VISIBLE);
			// homeTipButton.setText((String) msg.obj);
			// break;
			// 添加用户反馈功能
		case Constants.USER_FEEDBACK:
			// try {
			// // 用户反馈
			// String httpUri = "http://" +
			// ConfigApp.getInstance().getServerIp() +
			// ":8081/limesurvey/index.php/1/lang-zh-Hans";
			// // 非中文
			// if
			// (!ConfigApp.LANGUAGE_CN.equals(ConfigApp.getInstance().getCurLanguage()))
			// {
			// httpUri = "http://" + ConfigApp.getInstance().getServerIp() +
			// ":8081/limesurvey/index.php/1/lang-en";
			// }
			// Uri uri = Uri.parse(httpUri);
			// Intent it = new Intent(Intent.ACTION_VIEW, uri);
			// it.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			// startActivity(it);
			// Log.i(TAG, "try to give some Feedback");
			// } catch (ActivityNotFoundException e) {
			// Log.e(TAG, "no browser error");
			// showToastMsg(HomeActivity.this.getString(R.string.no_browser));
			// }
			break;
		case MSG_FOR_HOMEACTIVITY.MSG_NOTIFY_CALLCLOSE:
			removeCallComingActivity();
			// 由SettingActivity设置StatusHandler，但是没有写SettingActivity，所以这里没有设置当前状态的~
			// setSelfFieldStatus(ConfigAccount.getIns().getLoginAccount().getStatus());
			break;
		case Constants.MSG_ONREVTERMINATE:
			// showToastMsg(getString(R.string.module_error_1login));
			// setSelfStatus(SelfSettingWindow.AWAY);
			break;
		default:
			break;
		}
	}

	/**
	 * 注销程序
	 */
	private void logoutApp()
	{
		// ConfigApp.getInstance().setRestartEvent(2);
		// 在注销和登出时判断是否需要挂断,没设置VoipStatus,直接退出
		if (CallLogic.getIns().getVoipStatus() == CallLogic.STATUS_CLOSE)
		{
			logoutProcess();
			return;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(CallActivity.this);
		builder.setTitle("注销");
		builder.setMessage("您确定要注销吗？");
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.dismiss();
			}
		});
		builder.setNegativeButton("注销", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				Log.i(TAG, "logout~~");
				CallLogic.getIns().forceCloseCall();
				logoutProcess();
				dialog.dismiss();
			}

		});
		builder.create().show();
	}

	/**
	 * 登出
	 */
	private void logoutProcess()
	{

		// 注销后点击到注销 导致崩溃
		if (null == handler)
		{
			return;
		}
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				// 系统设置取消记住密码，注销以后再登录界面还会记住登录密码；
				Constants.setNeedToDelete(true);
				CallControl.getInstance().clear();
				App.getIns().logOut();
				backToLogin();
				LogSDK.setUser("");
			}
		}, 200);// 延时，如果在通话中不延时的话 显示不出进度框
	}

	/**
	 * 回到登录页面，并清理账号相关的数据
	 * 
	 * @param context
	 *            context对象， 用于处理 -6 被踢的 弹窗口
	 * @param errorCode
	 *            错误码 , 如果 == 0 不做提示处理
	 * @param desc
	 *            svn被踢描述由客户端提供，-6/-9时传null
	 */
	public void backToLogin()
	{
		ActivityStackManager.INSTANCE.loginOut();
		// setAutoReLogin(false);
		// 回到登录界面时去初始化Datamanager
		DataManager.getIns().uninit();
		// Intent intent = new Intent(this, LoginActivity.class);
		// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		// startActivity(intent);
	}

	/**
	 * 移除来电界面
	 */
	private void removeCallComingActivity()
	{
		Log.d(TAG, "removeCallComingActivity");
		// 不显示CallFragment的部分
		callAreaLayout.setVisibility(View.GONE);

		if (null != CallComingActivity.getInstance())
		{
			CallComingActivity.getInstance().finish();
		}
	}

	/**
	 * 更新呼叫界面显示内容
	 */
	public void showCallLayout()
	{
		Log.d(TAG, "showCallLayout()");
		// 设置呼叫界面可见
		callAreaLayout.setVisibility(View.VISIBLE);
		// //设置本地视频 欢迎界面 不可见
		// welcomeLayout.setVisibility(View.GONE);
		// confEnterLayout.setVisibility(View.GONE);
		// if (isFullScreen)
		// {
		// startFullScreen();
		// }
	}

	/**
	 * 收到挂断消息后操作
	 */
	public void closeCallBackToHome()
	{
		// 挂断 返回欢迎界面
		backToWelcome();
		// homeTipButton.setVisibility(View.GONE);
		CallLogic.getIns().setMainView(false);
		// 挂断时清空会场列表
		// Log.i(TAG, "clear confContacts");
		// dismissConferenceListView();
		// confFragment.enableConfBtn(true);
		// confContacts.clear();
		// isBroadcasted = false;
		// eSpaceService.getService().confManager.clearConfSiteInfosMap();
		// confListAdapter.setWatchedSiteInfoMTnum(null);
		// confListAdapter.setSelfMTNumber(null);
		// // 设置本地视频按钮可用
		// setPreviewBtnUserable();
		// confListAdapter.setIslocalBroadcast(TupBool.TUP_FALSE);
	}

	/**
	 * 关闭已打开的摄像头
	 */
	private void closeLocalCamera()
	{
		// previewVideoBtnLayout.setEnabled(false);
		// previewVideoBtn.setEnabled(false);
		// previewVideoBtn.setVisibility(View.GONE);
		// previewVideoBtn.setImageDrawable(getResources().getDrawable(R.drawable.te_mobile_home_camera_open));
		// if (localVideoAreaLayout.getVisibility() == View.VISIBLE)
		// {
		// //调用关闭摄像头方法
		// previewFragment.openOrCloseCamera(true);
		//
		// localVideoAreaLayout.setVisibility(View.GONE);
		// }
	}

	/**
	 * 挂断 返回欢迎界面
	 */
	public synchronized void backToWelcome()
	{
		Log.d(TAG, "backToWelcome() ");
		// dialFragment.reset();
		// 欢迎界面可见
		// welcomeLayout.setVisibility(View.VISIBLE);
		// confEnterLayout.setVisibility(View.VISIBLE);

		// 设置呼叫界面 本地视频 不可见
		callAreaLayout.setVisibility(View.GONE);
		// TODO Final，添加本地视频时需要设置下边两个
		// localVideoAreaLayout.setVisibility(View.GONE);
		// 设置本地视频按钮可用
		// setPreviewBtnUserable();

		// isFullScreen = true;
	}

	/*******************************************************************
	 * 处理HandlerMessage
	 *******************************************************************/

	/**
	 * 异步处理 广播事件
	 */
	private void handlerBroadcastEvent(final Intent intent)
	{
		if (intent != null)
		{
			String action = intent.getAction();
			if (CustomBroadcastConst.ACTION_CONNECT_TO_SERVER.equals(action))
			{
				// 在主页面时非主动操作，登录消息收到，1，断网重连了；2，在线心跳超时，重注册了。 ANDRIOD-195
				// 2013.11.26
				// 这两种情况都要进行挂断所有会话。TODO 是否需要增加提示？
				CallLogic.getIns().forceCloseCall();
				// end ANDRIOD-195 l00211010 2013.11.26
				// 关闭注册窗口并提示登录失败，如果成功的话则关闭，修改在线状态
				// dismissProgressDialog();// 将进度条先关闭
				// setSelfStatus(SelfSettingWindow.AWAY);
				// // begin add modify by l00211010 reason:注册失败需要设置离线状态
				// // DTS2014012301259
				// EspaceApp.getIns().setOnlineStatus(Constant.STATUS_OFFLINE);
				// end add modify by l00211010 reason:注册失败需要设置离线状态
				// DTS2014012301259
				String loginErrorType = intent.getStringExtra(Resource.SERVICE_ERROR_DATA);
				if (null != loginErrorType && !StringUtil.isStringEmpty(loginErrorType))
				{
					handleRequestError(loginErrorType);
				}

			} else if (CustomBroadcastConst.ACTION_LOGIN_RESPONSE.equals(action))
			{
				// dismissProgressDialog();
				// LogUI.i("loginSuccessResp in home");
				// loginSuccessResp();

				// ===
				// removeResponseFilter();
			} else if (CustomBroadcastConst.ACTION_REFRESHLICENSEFAILED_NOTIFY.equals(action))
			{
				Toast.makeText(CallActivity.this, "license保活失败,请重新注册license", Toast.LENGTH_LONG).show();
				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						CallLogic.getIns().forceCloseCall();
						logoutProcess();
					}
				}, 1500);
			}
		}
	}

	/**
	 * 
	 * 此方法用于登陆错误 弹窗展示
	 * 
	 * @param errorType
	 *            登陆错误类型
	 * @since 1.1
	 * @history 2013-9-10 v1.0.0 l00211010 create
	 */
	private void handleRequestError(final String errorType)
	{
		Log.w(TAG, "RequestError  errorType = " + errorType);
		int errorCode = 0;
		// 目前三种类型，鉴权失败，超时，服务器错误（作为服务器连接失败处理）
		if (errorType.equals(Resource.LOGIN_ACCOUNT_ERROR))
		{
			// 确认按钮点击事件
			DialogInterface.OnClickListener okClick = new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					// TODO 需要先注销并卸载框架

					logoutApp();
				}
			};
			// 在此处鉴权失败将推出到主界面
			this.showAlertDialog(null, getString(R.string.account_mistake), null, null, null, null, null);
			// 取消按钮点击事件
			return;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CONNECT_ERROR;
		}
		if (errorType.equals(Resource.LOGIN_SERVER_TIMEOUT))
		{
			errorCode = Resource.REQUEST_TIMEOUT;
		}
		if (errorType.equals(Resource.LOGIN_ACCOUNTNUM_OVERLIMIT))
		{
			errorCode = ResponseErrorCodeHandler.LOGIN_ACCOUNTNUM_OVERLIMIT;
		}
		// 用于修改登录成功后修改系统时间再次重新登录 证书过期认证失败时提示信息错误 原提示为终端请求异常
		if (errorType.equals(Resource.CERTIFICATE_ERROR))
		{
			errorCode = ResponseErrorCodeHandler.CERTIFICATE_ERROR;
		}
		if (errorType.equals(Resource.NETWORK_INVALID))
		{
			errorCode = ResponseErrorCodeHandler.NETWORK_INVALID;
		}
		ResponseErrorCodeHandler.handleRequestError(errorCode, this);
	}

	/**
	 * 方法名称：sendHandlerMessage 方法描述： 发送消息
	 */
	public void sendHandlerMessage(int what, Object object)
	{
		Log.i(TAG, "sendHandlerMessage exec ");
		Log.d(TAG, "handler->" + handler);
		Log.d(TAG, "what->" + what + "; object->" + object);
		if (handler == null)
		{
			Log.d(TAG, "sendHandlerMessage() handler is null");
			return;
		}
		Message msg = handler.obtainMessage(what, object);
		handler.sendMessage(msg);
	}

	/*******************************************************************
	 * 处理HandlerMessage finish
	 *******************************************************************/

	@Override
	public void onBackPressed()
	{
		super.moveTaskToBack(true);
	}

	@Override
	public void clearData()
	{
		if (null != handler)
		{
			handler.removeMessages(MSG_FOR_HOMEACTIVITY.MSG_LOGOUT_AND_REGISTE);
			handler = null;
		}

		unRegister();
		regReceiver = null;
		instance.ins = null;
	}

	/**
	 * 方法名称：sendHandlerMessage 方法描述： 发送消息
	 */
	public static void sendHandlerMessageByBroadcast(int what, Object object)
	{
		if (null == instance.ins)
		{
			Log.e(TAG, "CallActivity is null.");
			return;
		}
		instance.ins.sendHandlerMessage(what, object);
	}

	/**
	 * 类描述：登录页面的广播。
	 */
	private static class RegReceiver extends BroadcastReceiver
	{
		/**
		 * 界面广播接收
		 */
		@Override
		public void onReceive(Context context, Intent intent)
		{
			if (intent != null)
			{
				sendHandlerMessageByBroadcast(MSG_FOR_HOMEACTIVITY.BROADCAST_EVENT, intent);
			}
		}
	}

}