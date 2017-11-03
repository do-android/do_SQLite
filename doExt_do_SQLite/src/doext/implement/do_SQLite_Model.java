package doext.implement;

import java.io.IOException;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.text.TextUtils;
import core.DoServiceContainer;
import core.helper.DoIOHelper;
import core.helper.DoJsonHelper;
import core.interfaces.DoIScriptEngine;
import core.object.DoInvokeResult;
import doext.define.do_SQLite_IMethod;
import doext.define.do_SQLite_MAbstract;

/**
 * 自定义扩展MM组件Model实现，继承do_SQLite_MAbstract抽象类，并实现do_SQLite_IMethod接口方法；
 * #如何调用组件自定义事件？可以通过如下方法触发事件：
 * this.model.getEventCenter().fireEvent(_messageName, jsonResult);
 * 参数解释：@_messageName字符串事件名称，@jsonResult传递事件参数对象； 获取DoInvokeResult对象方式new
 * DoInvokeResult(this.getUniqueKey());
 */
public class do_SQLite_Model extends do_SQLite_MAbstract implements do_SQLite_IMethod {

	private SQLiteDatabase database;

	public do_SQLite_Model() throws Exception {
		super();
	}

	@Override
	public boolean onPropertiesChanging(Map<String, String> _changedValues) throws Exception {
		if (_changedValues.containsKey("path")) {
			String path = _changedValues.get("path");
			if ("".equals(path) || null == path) {
				return false;
			}
		}
		if (_changedValues.containsKey("sql")) {
			String sql = _changedValues.get("sql");
			if ("".equals(sql) || null == sql) {
				return false;
			}
		}
		return super.onPropertiesChanging(_changedValues);
	}

	/**
	 * 同步方法，JS脚本调用该组件对象方法时会被调用，可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public boolean invokeSyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		if ("open".equals(_methodName)) {
			open(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("close".equals(_methodName)) {
			close(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("executeSync".equals(_methodName)) {
			executeSync(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("executeSync1".equals(_methodName)) {
			executeSync1(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		if ("querySync".equals(_methodName)) {
			querySync(_dictParas, _scriptEngine, _invokeResult);
			return true;
		}
		return super.invokeSyncMethod(_methodName, _dictParas, _scriptEngine, _invokeResult);
	}

	/**
	 * 异步方法（通常都处理些耗时操作，避免UI线程阻塞），JS脚本调用该组件对象方法时会被调用， 可以根据_methodName调用相应的接口实现方法；
	 * 
	 * @_methodName 方法名称
	 * @_dictParas 参数（K,V）
	 * @_scriptEngine 当前page JS上下文环境
	 * @_callbackFuncName 回调函数名 #如何执行异步方法回调？可以通过如下方法：
	 *                    _scriptEngine.callback(_callbackFuncName,
	 *                    _invokeResult);
	 *                    参数解释：@_callbackFuncName回调函数名，@_invokeResult传递回调函数参数对象；
	 *                    获取DoInvokeResult对象方式new
	 *                    DoInvokeResult(this.getUniqueKey());
	 */
	@Override
	public boolean invokeAsyncMethod(String _methodName, JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		if ("execute".equals(_methodName)) {
			execute(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("execute1".equals(_methodName)) {
			execute1(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		if ("query".equals(_methodName)) {
			query(_dictParas, _scriptEngine, _callbackFuncName);
			return true;
		}
		return super.invokeAsyncMethod(_methodName, _dictParas, _scriptEngine, _callbackFuncName);
	}

	/**
	 * 打开数据库；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void open(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String path = DoJsonHelper.getString(_dictParas, "path", "");
		if ("".equals(path) || null == path) {
			_invokeResult.setResultBoolean(false);
			DoServiceContainer.getLogEngine().writeInfo("SQLite", "打开数据库失败：path" + path);
		} else {
			try {
				if (":memory:".equalsIgnoreCase(path)) {
					database = SQLiteDatabase.create(null);
				} else {
					String dbPath = _scriptEngine.getCurrentApp().getDataFS().getFileFullPathByName(path);
					createDBFile(dbPath);
					database = SQLiteDatabase.openOrCreateDatabase(dbPath, null);
				}
				_invokeResult.setResultBoolean(true);
			} catch (Exception _err) {
				_invokeResult.setResultBoolean(false);
				DoServiceContainer.getLogEngine().writeError("SQLite打开数据库失败", _err);
			}
		}
	}

	/**
	 * 创建数据库DB文件
	 * 
	 * @param dbPath
	 * @throws IOException
	 */
	private void createDBFile(String dbPath) throws IOException {
		String _directory = dbPath.substring(0, dbPath.lastIndexOf("/"));
		DoIOHelper.createDirectory(_directory);
		DoIOHelper.createFile(dbPath);
	}

	/**
	 * 关闭数据库；
	 * 
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_invokeResult 用于返回方法结果对象
	 */
	@Override
	public void close(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) {
		try {
			if (null != database) {
				database.close();
			}
			_invokeResult.setResultBoolean(true);
		} catch (Exception _err) {
			_invokeResult.setResultBoolean(false);
			DoServiceContainer.getLogEngine().writeError("SQLite关闭数据库失败", _err);
		}
	}

	/**
	 * 执行SQL语句；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void executeSync(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _sql = DoJsonHelper.getString(_dictParas, "sql", "").trim();
		if (TextUtils.isEmpty(_sql)) {
			throw new Exception("执行SQL失败，sql is empty");
		}
		JSONArray _bind = DoJsonHelper.getJSONArray(_dictParas, "bind");
		try {
			execSQL(_sql, _bind);
			_invokeResult.setResultBoolean(true);
		} catch (Exception _err) {
			_invokeResult.setResultBoolean(false);
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		}
	}

	/**
	 * 执行SQL语句；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void execute(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _sql = DoJsonHelper.getString(_dictParas, "sql", "").trim();
		if (TextUtils.isEmpty(_sql)) {
			throw new Exception("执行SQL失败，sql is empty");
		}
		JSONArray _bind = DoJsonHelper.getJSONArray(_dictParas, "bind");
		DoInvokeResult invokeResult = new DoInvokeResult(getUniqueKey());
		try {
			execSQL(_sql, _bind);
			invokeResult.setResultBoolean(true);
		} catch (Exception _err) {
			invokeResult.setResultBoolean(false);
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		} finally {
			_scriptEngine.callback(_callbackFuncName, invokeResult);
		}
	}

	//只支持单条
	private void execSQL(String sql, JSONArray bind) throws JSONException {
		if (database == null) {
			throw new RuntimeException("SQLite database==null，没有打开或创建数据库");
		}
		Object[] _bindArgs = null;
		int _length = 0;
		if (bind != null) {
			_length = bind.length();
			_bindArgs = new Object[_length];
			for (int i = 0; i < _length; i++) {
				_bindArgs[i] = bind.get(i);
			}
		}
		if (_bindArgs == null) {
			database.execSQL(sql);
		} else {
			database.execSQL(sql, _bindArgs);
		}
	}

	private JSONArray getQueryResult(Cursor cursor, String sql) throws Exception {
		int count = cursor.getCount();
		JSONArray rows = new JSONArray();
		if (cursor.moveToFirst()) {
			String[] columnNames = cursor.getColumnNames();
			for (int i = 0; i < count; i++) {
				cursor.moveToPosition(i);
				JSONObject colNode = new JSONObject();
				for (int j = 0; j < columnNames.length; j++) {
					String columnName = columnNames[j];
					String value = cursor.getString(cursor.getColumnIndex(columnName));
					colNode.put(columnName, value);
				}
				rows.put(colNode);
			}
		}
		return rows;
	}

	/**
	 * 执行SQL查询语句；
	 * 
	 * @throws Exception
	 * @_dictParas 参数（K,V），可以通过此对象提供相关方法来获取参数值（Key：为参数名称）；
	 * @_scriptEngine 当前Page JS上下文环境对象
	 * @_callbackFuncName 回调函数名
	 */
	@Override
	public void query(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		String _sql = DoJsonHelper.getString(_dictParas, "sql", "").trim();
		if (TextUtils.isEmpty(_sql)) {
			throw new Exception("SQLite查询失败，sql is empty");
		}
		JSONArray _bind = DoJsonHelper.getJSONArray(_dictParas, "bind");
		DoInvokeResult invokeResult = new DoInvokeResult(getUniqueKey());
		Cursor cursor = null;
		try {
			cursor = rawQuery(_sql, _bind);
			invokeResult.setResultArray(getQueryResult(cursor, _sql));
		} catch (Exception _err) {
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		} finally {
			_scriptEngine.callback(_callbackFuncName, invokeResult);
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	@Override
	public void querySync(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		String _sql = DoJsonHelper.getString(_dictParas, "sql", "").trim();
		if (TextUtils.isEmpty(_sql)) {
			throw new Exception("SQLite查询失败，sql is empty");
		}
		JSONArray _bind = DoJsonHelper.getJSONArray(_dictParas, "bind");
		Cursor cursor = null;
		try {
			cursor = rawQuery(_sql, _bind);
			_invokeResult.setResultArray(getQueryResult(cursor, _sql));
		} catch (Exception _err) {
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		} finally {
			if (cursor != null && !cursor.isClosed()) {
				cursor.close();
			}
		}
	}

	//只支持单条
	private Cursor rawQuery(String sql, JSONArray bind) throws JSONException {
		if (database == null) {
			throw new RuntimeException("SQLite database==null，没有打开或创建数据库");
		}
		Cursor cursor = null;
		String[] _bindArgs = null;
		int _length = 0;
		if (bind != null) {
			_length = bind.length();
			_bindArgs = new String[_length];
			for (int i = 0; i < _length; i++) {
				_bindArgs[i] = bind.getString(i);
			}
		}
		if (_bindArgs == null) {
			cursor = database.rawQuery(sql, new String[] {});
		} else {
			cursor = database.rawQuery(sql, _bindArgs);
		}

		return cursor;
	}

	@Override
	public void executeSync1(JSONObject _dictParas, DoIScriptEngine _scriptEngine, DoInvokeResult _invokeResult) throws Exception {
		JSONArray _sqls = DoJsonHelper.getJSONArray(_dictParas, "sqls");
		boolean _isTranaction = DoJsonHelper.getBoolean(_dictParas, "isTransaction", false);
		String _resultCount = "0";
		try {
			if (database == null) {
				throw new RuntimeException("SQLite database==null，没有打开或创建数据库");
			}
			_resultCount = execSQLByTransaction(_sqls, _isTranaction) + "";
		} catch (Exception _err) {
			_invokeResult.setError(_err.getMessage());
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		} finally {
			_invokeResult.setResultInteger(Integer.parseInt(_resultCount));
		}
	}

	@Override
	public void execute1(JSONObject _dictParas, DoIScriptEngine _scriptEngine, String _callbackFuncName) throws Exception {
		JSONArray _sqls = DoJsonHelper.getJSONArray(_dictParas, "sqls");
		boolean _isTranaction = DoJsonHelper.getBoolean(_dictParas, "isTransaction", false);
		String _resultCount = "0";
		DoInvokeResult invokeResult = new DoInvokeResult(getUniqueKey());
		try {
			if (database == null) {
				throw new RuntimeException("SQLite database==null，没有打开或创建数据库");
			}
			_resultCount = execSQLByTransaction(_sqls, _isTranaction) + "";
		} catch (Exception _err) {
			invokeResult.setError(_err.getMessage());
			DoServiceContainer.getLogEngine().writeError("SQLite", _err);
		} finally {
			invokeResult.setResultInteger(Integer.parseInt(_resultCount));
			_scriptEngine.callback(_callbackFuncName, invokeResult);
		}

	}

	private long execSQLByTransaction(JSONArray _sqls, boolean _isTransaction) throws JSONException {
		if (_sqls == null || _sqls.length() <= 0) {
			return 0;
		}
		long _resultCount = 0;
		if (_isTransaction) {
			database.beginTransaction();
			try {
				_resultCount = execSQLs(_sqls, _isTransaction);
				database.setTransactionSuccessful();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				database.endTransaction();
			}
		} else {
			_resultCount = execSQLs(_sqls, _isTransaction);
		}

		return _resultCount;
	}

	private long execSQLs(JSONArray _sqls, boolean _isTransaction) throws JSONException {
		long _resultCount = 0;
		for (int i = 0; i < _sqls.length(); i++) {
			String _sql = _sqls.getString(i).trim();
			String _sql_prefix = "";
			if (_sql.length() >= 6) {
				_sql_prefix = _sql.substring(0, 6);
			}
			long _rowCount = 0;
			SQLiteStatement _statement = database.compileStatement(_sql);
			if ("insert".equalsIgnoreCase(_sql_prefix)) {
				try {
					long _rowID = _statement.executeInsert();
					if (_rowID > 0) {
						_rowCount++;
					}
				} catch (SQLException e) {
					if (_isTransaction) {//如果执行错误会抛出异常，事务需要回滚，所以正常抛异常
						throw new SQLException("执行sql : " + _sql + " 出错！" + e.getMessage());
					} else {//不是事务的话不需要抛异常，需要继续往下执行
						DoServiceContainer.getLogEngine().writeError("do_SQLite", new SQLException("执行sql : " + _sql + " 出错！" + e.getMessage()));
					}
				}
			} else if ("update".equalsIgnoreCase(_sql_prefix) || "delete".equalsIgnoreCase(_sql_prefix)) {
				_rowCount = _statement.executeUpdateDelete();
			}else{
				database.execSQL(_sql);
			}
//			if (_rowCount == 0) { //不算执行失败，输出日志提示
//				DoServiceContainer.getLogEngine().writeInfo("执行sql : " + _sql + " 出错！", "do_SQLite");
//			}
			_resultCount += _rowCount;
		}
		return _resultCount;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (null != database) {
			database.close();
		}
	}

}