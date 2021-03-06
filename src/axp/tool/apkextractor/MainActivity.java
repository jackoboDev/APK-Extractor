package axp.tool.apkextractor;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

public class MainActivity extends AppCompatActivity {
	private ApkListAdapter apkListAdapter;

	private ProgressBar progressBar;
	private PermissionResolver permissionResolver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		setSupportActionBar((Toolbar)findViewById(R.id.toolbar));

		RecyclerView listView = (RecyclerView)findViewById(android.R.id.list);

		apkListAdapter = new ApkListAdapter(this);
		listView.setLayoutManager(new LinearLayoutManager(this));
		listView.setAdapter(apkListAdapter);

		progressBar = (ProgressBar) findViewById(android.R.id.progress);
		progressBar.setVisibility(View.VISIBLE);

		new Loader(this).execute();

		permissionResolver = new PermissionResolver(this);
	}



	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (!permissionResolver.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
			super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		}
	}

	public void hideProgressBar() {
		progressBar.setVisibility(View.GONE);
	}

	public void addItem(PackageInfo item) {
		apkListAdapter.addItem(item);
	}

	public void doExctract(final PackageInfo info) {
		if (!permissionResolver.resolve()) return;

		final Extractor extractor = new Extractor();
		try {
			String dst = extractor.extractWithoutRoot(info);
			Toast.makeText(this, String.format(this.getString(R.string.toast_extracted), dst), Toast.LENGTH_SHORT).show();
			return;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		new AlertDialog.Builder(this)
			.setTitle(R.string.alert_root_title)
			.setMessage(R.string.alert_root_body)
			.setPositiveButton(R.string.alert_root_yes, (dialog, which) -> {
				try {
					String dst = extractor.extractWithRoot(info);
					Toast.makeText(MainActivity.this, String.format(MainActivity.this.getString(R.string.toast_extracted), dst), Toast.LENGTH_SHORT).show();
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(MainActivity.this, R.string.toast_failed, Toast.LENGTH_SHORT).show();
				}
			}).setNegativeButton(R.string.alert_root_no, null)
			.show();
	}

	@SuppressLint("StaticFieldLeak")
	class Loader extends AsyncTask<Void, PackageInfo, Void> {
		MainActivity   mainActivity;

		public Loader(MainActivity a) {
			mainActivity = a;
		}

		@Override
		protected Void doInBackground(Void... params) {
			List<PackageInfo> packages = getPackageManager().getInstalledPackages(PackageManager.GET_META_DATA);
			for (PackageInfo packageInfo : packages) {
				publishProgress(packageInfo);
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(PackageInfo... values) {
			super.onProgressUpdate(values);
			mainActivity.addItem(values[0]);
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
		}
	}
}
