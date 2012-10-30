package net.sunky.smarttags.writer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;


public class MainAct extends Activity {
	NfcAdapter AdpNfc;
	PendingIntent PINfc;
	IntentFilter IFNfc[];
	boolean bWriteMode = false;
	Tag TagNfc;
	Context Ctx;
	TextView tvShow;
	ImageButton btnBlue;
	ImageButton btnRed;
	ImageButton btnBlack;
	ImageButton btnWhite;
	Button      btnShare;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_main);
		tvShow = (TextView) findViewById(R.id.tvDisplay);
		btnBlue = (ImageButton) findViewById(R.id.btBlue);
		btnRed = (ImageButton) findViewById(R.id.btRed);
		btnBlack = (ImageButton) findViewById(R.id.btBlack);
		btnWhite = (ImageButton) findViewById(R.id.btWhite);
		btnShare = (Button) findViewById(R.id.btn_share);
		Ctx = this;
		disableButtons();
		btnShare.setVisibility(View.INVISIBLE);

		AdpNfc = NfcAdapter.getDefaultAdapter(this);
		if (AdpNfc == null) {
			tvShow.setText(Ctx.getString(R.string.info_nonfc));
			return;
		}
		if (!AdpNfc.isEnabled()) {
			tvShow.setText(Ctx.getString(R.string.info_nfcdisabled));
			return;
		}

		PINfc = PendingIntent.getActivity(this, 0, new Intent(this, getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
		IntentFilter IFTagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		IFTagDetected.addCategory(Intent.CATEGORY_DEFAULT);
		IFNfc = new IntentFilter[] { IFTagDetected };

		tvShow.setText(Ctx.getString(R.string.info_touch));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.layout_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_share:
			menuShare();
			return true;
		case R.id.menu_about:
			menuAbout();
			return true;
		case R.id.menu_help:
			menuHelp();
			return true;
		case R.id.menu_exit:
			menuExit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void writeBlue(View view) {
		writeCommon("semc://liveware/A1/1/NT1/1/smarttags1");
	}

	public void writeRed(View view) {
		writeCommon("semc://liveware/A1/1/NT1/2/smarttags1");
	}

	public void writeBlack(View view) {
		writeCommon("semc://liveware/A1/1/NT1/3/smarttags1");
	}

	public void writeWhite(View view) {
		writeCommon("semc://liveware/A1/1/NT1/4/smarttags1");
	}

	public void shareButton(View view) {
		menuShare();
	}
	
	public void menuShare() {
		Intent it = new Intent(Intent.ACTION_SEND);
		it.setType("image/*");
		it.putExtra(Intent.EXTRA_SUBJECT, Ctx.getString(R.string.menu_share));
		it.putExtra(Intent.EXTRA_TEXT, Ctx.getString(R.string.info_share));
		it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(Intent.createChooser(it, getTitle()));
	}

	public void menuAbout() {
		Uri uriHelp = Uri.parse(Ctx.getString(R.string.url_about));
		Intent it = new Intent(Intent.ACTION_VIEW, uriHelp);
		startActivity(it);
	}

	public void menuExit() {
		finish();
	}

	public void menuHelp() {
		Uri uriHelp = Uri.parse(Ctx.getString(R.string.url_help));
		Intent it = new Intent(Intent.ACTION_VIEW, uriHelp);
		startActivity(it);
	}

	private void writeCommon(String strNfcMsg) {
		try {
			disableButtons();
			if (TagNfc == null) {
				tvShow.setText(Ctx.getString(R.string.info_detected_error));
			} else {
				tvShow.setText(Ctx.getString(R.string.info_writing));
				writeNfc(strNfcMsg, TagNfc);
				tvShow.setText(Ctx.getString(R.string.info_writed));
				tvShow.performHapticFeedback(
						HapticFeedbackConstants.LONG_PRESS,
						HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
				tvShow.playSoundEffect(SoundEffectConstants.CLICK);
				btnShare.setVisibility(View.VISIBLE);
			}
		} catch (IOException e) {
			tvShow.setText(Ctx.getString(R.string.info_write_fail));
			e.printStackTrace();
		} catch (FormatException e) {
			tvShow.setText(Ctx.getString(R.string.info_write_fail));
			e.printStackTrace();
		}
	}

	private void writeNfc(String strNfcMsg, Tag tagNfc) throws IOException,
			FormatException {
		NdefRecord[] ndefRecord = { createNdefUriRecord(strNfcMsg) };
		NdefMessage ndefMsg = new NdefMessage(ndefRecord);

		Ndef ndef = Ndef.get(tagNfc);
		ndef.connect();
		ndef.writeNdefMessage(ndefMsg);
		ndef.close();
	}

	private NdefRecord createNdefUriRecord(String strNfcMsg)
			throws UnsupportedEncodingException {
		byte[] uriField = strNfcMsg.getBytes(Charset.forName("US-ASCII"));
		byte[] payload = new byte[uriField.length + 1];
		System.arraycopy(uriField, 0, payload, 1, uriField.length);
		NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
				NdefRecord.RTD_URI, new byte[0], payload);

		return recordNFC;
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			TagNfc = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
			tvShow.setText(Ctx.getString(R.string.info_detected));
			enableButtons();
			tvShow.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS,
					HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING);
			tvShow.playSoundEffect(SoundEffectConstants.NAVIGATION_UP);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		writeModeOff();
	}

	@Override
	public void onResume() {
		super.onResume();
		writeModeOn();
	}

	private void writeModeOn() {
		bWriteMode = true;
		if (AdpNfc == null)
			return;
		if (!AdpNfc.isEnabled())
			return;
		AdpNfc.enableForegroundDispatch(this, PINfc, IFNfc, null);
	}

	private void writeModeOff() {
		bWriteMode = false;
		if (AdpNfc == null)
			return;
		if (!AdpNfc.isEnabled())
			return;
		AdpNfc.disableForegroundDispatch(this);
	}

	private void disableButtons() {
		setButtons(false);
	}

	private void enableButtons() {
		setButtons(true);
	}

	private void setButtons(boolean bClickable) {
		btnBlue.setClickable(bClickable);
		btnRed.setClickable(bClickable);
		btnBlack.setClickable(bClickable);
		btnWhite.setClickable(bClickable);
	}
}
