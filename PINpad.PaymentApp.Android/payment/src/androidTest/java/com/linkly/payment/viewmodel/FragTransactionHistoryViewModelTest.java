package com.linkly.payment.viewmodel;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static com.linkly.libengine.engine.EngineManager.TransType.SALE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import androidx.lifecycle.MutableLiveData;
import androidx.test.annotation.UiThreadTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.linkly.libengine.config.Config;
import com.linkly.libengine.config.PayCfg;
import com.linkly.libengine.config.PayCfgFactory;
import com.linkly.libengine.dependencies.Dependencies;
import com.linkly.libengine.engine.Engine;
import com.linkly.libengine.engine.transactions.TransRec;
import com.linkly.libengine.status.StatusReport;
import com.linkly.libmal.MalFactory;
import com.linkly.libpositive.messages.Messages;
import com.linkly.payment.viewmodel.data.UIFragData;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class FragTransactionHistoryViewModelTest {

    private final FragTransactionHistoryViewModel testSubject = new FragTransactionHistoryViewModel(getApplicationContext());

    Dependencies d;

    @Before
    public void setUp() throws Exception {
        // set up a minimal set of dependencies to run the tests
        d = new Dependencies();
        d.setConfig(Config.getInstance());
        // This would be better accessed via Config, which itself holds PayCfg as a member. See:
        //  https://linkly.atlassian.net/browse/TT-369
        d.setPayCfg(Config.getInstance().getPayCfg());
        d.setStatusReporter(StatusReport.getInstance());
        d.setMessages(Messages.getInstance());
        //d.setPayCfg(new PayCfgFactory().getConfig(getApplicationContext()));
        Engine.init(d, InstrumentationRegistry.getInstrumentation().getTargetContext(), MalFactory.getInstance());
    }

    @Test
    public void updateViewModel() {
        MutableLiveData<UIFragData> fragData = new MutableLiveData<>();
        testSubject.updateViewModel(fragData);
        assertEquals(fragData, testSubject.fragData);
    }

}