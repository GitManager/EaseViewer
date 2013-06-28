package team.androidreader.mail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;

import team.androidreader.dialog.WaittingDialog;
import team.androidreader.utils.OnProgressListener;
import team.top.activity.R;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class SendActivity extends Activity implements OnProgressListener {

	private EditText subject;
	private EditText body;
	private TextView attach;
	private Button send;
	private String userAddress;
	private Dialog inputDialog;
	private Button ensure;
	private EditText passwdEditext;
	private List<String> recipients;
	private String mailServlet;
	private static final int PASSWD_ERROR = 0;
	private static final int CAN_NOT_FOUND_SERVLET = 1;
	private static final int CONNECT_SUCCESS = 2;
	private String passwd;
	private String subContent;
	private String bodyContent;
	private WaittingDialog waittingDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sendmail);
		Intent intent = getIntent();
		userAddress = intent.getStringExtra("useraddress");
		mailServlet = intent.getStringExtra("servlet");
		Bundle recipientsBundle = intent.getExtras();
		recipients = recipientsBundle.getStringArrayList("recipients");
		waittingDialog = new WaittingDialog(this);
		
		init();
	}

	private void init() {
		subject = (EditText) findViewById(R.id.subject);
		body = (EditText) findViewById(R.id.body);
		attach = (TextView) findViewById(R.id.tvattach);
		send = (Button) findViewById(R.id.sendmail);
		inputDialog = new Dialog(SendActivity.this);
		inputDialog.setTitle(R.string.promot_enter_passwd);
		inputDialog.setContentView(R.layout.dialog_input);
		ensure = (Button) inputDialog.findViewById(R.id.ensure);
		passwdEditext = (EditText) inputDialog.findViewById(R.id.useraddress);
		passwdEditext.setTransformationMethod(PasswordTransformationMethod
				.getInstance());
		send.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				inputDialog.show();
				passwdEditext.setText("");
			}
		});

		ensure.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				subContent = subject.getText().toString();
				bodyContent = body.getText().toString();
				passwd = passwdEditext.getText().toString();

				if (!checkNetworkState()) {
					Toast.makeText(SendActivity.this,
							R.string.net_work_connect_failed,
							Toast.LENGTH_SHORT).show();
					return;
				}

				if (passwd.equals("")) {
					Toast.makeText(SendActivity.this,
							R.string.promot_enter_passwd, Toast.LENGTH_SHORT)
							.show();
					return;
				}
				inputDialog.cancel();
				SendActivity.this.OnProgressStart();

			}
		});
	}

	private boolean checkNetworkState() {
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connManager.getActiveNetworkInfo() != null) {
			return connManager.getActiveNetworkInfo().isAvailable();
		}
		return false;
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			SendActivity.this.OnProgressFinished();
			int what = msg.what;
			switch (what) {
			case PASSWD_ERROR:
				Toast.makeText(SendActivity.this, R.string.passwd_incorrect,
						Toast.LENGTH_SHORT).show();
				break;
			case CAN_NOT_FOUND_SERVLET:
				Toast.makeText(SendActivity.this,
						R.string.net_work_connect_failed, Toast.LENGTH_SHORT)
						.show();
				break;
			case CONNECT_SUCCESS:
				Intent intent = new Intent();
				Bundle bundle = new Bundle();
				bundle.putStringArrayList("recipients",
						(ArrayList<String>) recipients);
				intent.putExtra("subject", subContent);
				intent.putExtra("body", bodyContent);
				intent.putExtra("servlet", mailServlet);
				intent.putExtra("passwd", passwd);
				intent.putExtra("useraddress", userAddress);
				intent.putExtras(bundle);
				intent.setClass(SendActivity.this, MailSendService.class);
				startService(intent);
				break;
			default:
				break;
			}
			
		}

	};

	@Override
	public void OnProgressStart() {
		Thread thread = new Thread(new CheckPasswdThread());
		thread.start();
		waittingDialog.show();
		waittingDialog.setText(R.string.dialog_waitting_check);
	}

	@Override
	public void OnProgressFinished() {
		waittingDialog.cancel();
	}

	class CheckPasswdThread implements Runnable {

		@Override
		public void run() {
			try {
				Properties props = new Properties();
				props.put("mail.smtp.host", mailServlet);
				props.put("mail.smtp.port", String.valueOf(25));
				props.put("mail.smtp.auth", "true");
				Transport transport = null;
				Session session = Session.getDefaultInstance(props, null);
				transport = session.getTransport("smtp");
				transport.connect(mailServlet, userAddress, passwd);
				transport.close();
				handler.sendEmptyMessage(CONNECT_SUCCESS);
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(CAN_NOT_FOUND_SERVLET);
			} catch (MessagingException e) {
				e.printStackTrace();
				handler.sendEmptyMessage(PASSWD_ERROR);
			}
		
		}

	}
}
