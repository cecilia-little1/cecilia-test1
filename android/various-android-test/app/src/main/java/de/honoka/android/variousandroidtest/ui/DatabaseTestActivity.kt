package de.honoka.android.variousandroidtest.ui

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import cn.hutool.core.util.RandomUtil
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper
import com.j256.ormlite.dao.Dao
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.support.ConnectionSource
import com.j256.ormlite.table.DatabaseTable
import com.j256.ormlite.table.TableUtils
import de.honoka.android.variousandroidtest.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.RoundingMode
import java.util.*

class DatabaseTestActivity : AppCompatActivity() {

    companion object {

        lateinit var dao: Dao<TestEntity, String>
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_database_test)
        CoroutineScope(Dispatchers.IO).launch {
            doAction()
        }
    }

    private fun doAction() {
        val databaseHelper = DatabaseHelper(this)
        dao = databaseHelper.getDao(TestEntity::class.java)
        //transactionTest()
        val queryForAll = dao.queryForAll()
        println(queryForAll)
    }

    private fun transactionTest() {
        try {
            dao.callBatchTasks {
                val items = TestEntity.insertItems(5)
                if(items.last().col1!! > 80) throw Exception()
            }
        } catch(t: Throwable) {
            println()
        }
    }
}

class DatabaseHelper(context: Context) : OrmLiteSqliteOpenHelper(
    context, "database_test.db", null, 2
) {

    override fun onCreate(database: SQLiteDatabase, connectionSource: ConnectionSource) {
        TableUtils.createTable(connectionSource, TestEntity::class.java)
        TestEntity.insertItems(10)
    }

    override fun onUpgrade(
        database: SQLiteDatabase,
        connectionSource: ConnectionSource,
        oldVersion: Int,
        newVersion: Int
    ) {
        TableUtils.dropTable<TestEntity, Any>(connectionSource, TestEntity::class.java, false)
        TableUtils.createTable(connectionSource, TestEntity::class.java)
        TestEntity.insertItems(5)
        //throw Exception()
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        println()
    }
}

@DatabaseTable(tableName = "test_entity")
data class TestEntity(

    @DatabaseField(id = true)
    var id: String? = null,

    @DatabaseField
    var col1: Int? = null,

    @DatabaseField
    var col2: Boolean? = null,

    @DatabaseField
    var col3: Double? = null
) {

    companion object {

        fun insertItems(count: Int): List<TestEntity> {
            val list = ArrayList<TestEntity>()
            repeat(count) {
                DatabaseTestActivity.dao.create(TestEntity().apply {
                    id = UUID.randomUUID().toString()
                    col1 = RandomUtil.randomInt(1, 100)
                    col2 = col1!! / 2 == 0
                    col3 = RandomUtil.randomDouble(1.0, 100.0, 2, RoundingMode.HALF_UP)
                    list.add(this)
                })
            }
            return list
        }
    }
}