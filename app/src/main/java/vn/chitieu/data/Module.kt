package vn.chitieu.data
import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton
@Module @InstallIn(SingletonComponent::class) object DataModule {
 @Provides @Singleton fun database(@ApplicationContext context:Context):LedgerDb=Room.databaseBuilder(context,LedgerDb::class.java,"chitieu.db").addMigrations(MIGRATION_1_2).build()
}
