package tabfragment;


import android.content.Intent;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ToxicBakery.viewpager.transforms.ABaseTransformer;
import com.ToxicBakery.viewpager.transforms.AccordionTransformer;
import com.ToxicBakery.viewpager.transforms.BackgroundToForegroundTransformer;
import com.ToxicBakery.viewpager.transforms.CubeInTransformer;
import com.ToxicBakery.viewpager.transforms.CubeOutTransformer;
import com.ToxicBakery.viewpager.transforms.DefaultTransformer;
import com.ToxicBakery.viewpager.transforms.DepthPageTransformer;
import com.ToxicBakery.viewpager.transforms.FlipHorizontalTransformer;
import com.ToxicBakery.viewpager.transforms.FlipVerticalTransformer;
import com.ToxicBakery.viewpager.transforms.ForegroundToBackgroundTransformer;
import com.ToxicBakery.viewpager.transforms.RotateDownTransformer;
import com.ToxicBakery.viewpager.transforms.RotateUpTransformer;
import com.ToxicBakery.viewpager.transforms.StackTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomInTransformer;
import com.ToxicBakery.viewpager.transforms.ZoomOutTranformer;
import com.bigkoo.convenientbanner.ConvenientBanner;
import com.bigkoo.convenientbanner.holder.CBViewHolderCreator;
import com.example.administrator.materialmanagement.R;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ui.AgentRetailer;
import ui.DlsLss;
import ui.OffLineCk;
import ui.OffLineCkHistory;
import ui.OutLibraryStepOne;
import ui.PreventFlee;
import utile.BaseFragment;
import utile.LocalImageHolderView;
import utile.NetUtils;
import utile.SharedPrefsUtil;
import utile.ShowToast;

import static com.example.administrator.materialmanagement.Login.AGENT_CODE;
import static com.example.administrator.materialmanagement.Login.ONE_CODE;
import static com.example.administrator.materialmanagement.Login.SALE_CODE;

/**
 * A simple {@link Fragment} subclass.
 */
public class Workbench extends BaseFragment {
    private View view;
    private String code;
    @BindView(R.id.title_name)
    TextView title_name;
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.homeBanner)
    ConvenientBanner homeBanner;
    //banner加载的图片集
    private ArrayList<Integer> localImages;
    //翻页效果集
    private ArrayList<String> transformerList;
    private ABaseTransformer transforemer;
    private String transforemerName;
    private Class cls;
    @BindView(R.id.ckcx_ll)
    LinearLayout ckcx_ll;
    @BindView(R.id.ckls_tv)
    TextView ckls_tv;
    @BindView(R.id.tk_ll)
    LinearLayout tk_ll;
    @BindView(R.id.tk_tv)
    TextView tk_tv;
    @BindView(R.id.tkls_ll)
    LinearLayout tkls_ll;
    @BindView(R.id.tkls_tv)
    TextView tkls_tv;
    @BindView(R.id.ck_ll)
    LinearLayout ck_ll;
    @BindView(R.id.ck_tv)
    TextView ck_tv;
    @BindView(R.id.dls_ll)
    LinearLayout dls_ll;
    @BindView(R.id.dls_tv)
    TextView dls_tv;
    @BindView(R.id.lxck_ll)
    LinearLayout lxck_ll;
    @BindView(R.id.lxckls_ll)
    LinearLayout lxckls_ll;

    @BindView(R.id.tiaoji_ll)
    LinearLayout tiaoji_ll;
    @BindView(R.id.tiaojihis_ll)
    LinearLayout tiaojihis_ll;
    @BindView(R.id.preventflee_ll)
    LinearLayout preventflee_ll;


    @Override
    public View makeView() {
        view = View.inflate(getActivity(), R.layout.fragment_workbench, null);
        //绑定fragment
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    protected void loadData() {
        title_name.setText(R.string.gzt);
        back.setVisibility(View.GONE);
        setHomeBanner();
        if (SharedPrefsUtil.getValue(getActivity(), "userInfo", "usertype", "").equals(ONE_CODE)) {
            ck_tv.setText(R.string.yjck);
            tk_tv.setText(R.string.yjtk);
            dls_tv.setText(R.string.dls);
            ckls_tv.setText(R.string.yjckls);
            tkls_tv.setText(R.string.yjtkls);
            tiaoji_ll.setVisibility(View.GONE);
        } else if (SharedPrefsUtil.getValue(getActivity(), "userInfo", "usertype", "").equals(SALE_CODE)) {
            ckcx_ll.setVisibility(View.GONE);
            tkls_ll.setVisibility(View.GONE);
            ck_ll.setVisibility(View.GONE);
            tk_ll.setVisibility(View.GONE);
            dls_ll.setVisibility(View.GONE);
            lxck_ll.setVisibility(View.GONE);
            lxckls_ll.setVisibility(View.GONE);
            tiaoji_ll.setVisibility(View.GONE);
            tiaojihis_ll.setVisibility(View.GONE);
        } else if (SharedPrefsUtil.getValue(getActivity(), "userInfo", "usertype", "").equals(AGENT_CODE)) {
            ck_tv.setText(R.string.jxck);
            tk_tv.setText(R.string.jxtk);
            dls_tv.setText(R.string.lss);
            ckls_tv.setText(R.string.jxckls);
            tkls_tv.setText(R.string.jxtkls);
            tkls_ll.setVisibility(View.GONE);
            tk_ll.setVisibility(View.GONE);
        }
    }


    /**
     * 设置轮播
     */
    private void setHomeBanner() {
        localImages = new ArrayList<Integer>();
        transformerList = new ArrayList<String>();

        for (int position = 1; position < 4; position++) {
            localImages.add(getResId("banner" + position, R.drawable.class));
        }

        //自定义Holder
        homeBanner.setPages(
                new CBViewHolderCreator<LocalImageHolderView>() {
                    @Override
                    public LocalImageHolderView createHolder() {
                        return new LocalImageHolderView();
                    }
                }, localImages)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.drawable.ic_page_indicator, R.drawable.ic_page_indicator_focused})
                //设置指示器的方向
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.CENTER_HORIZONTAL);
        // 设置翻页的效果，不需要翻页效果可用不设

//                .setPageTransformer(Transformer.CubeIn);
//        convenientBanner.setManualPageable(false);//设置不能手动影响


        //加载网络图片
//        networkImages= Arrays.asList(imageUrls);
//        banner.setPages(new CBViewHolderCreator<NetworkImageHolderView>() {
//            @Override
//            public NetworkImageHolderView createHolder() {
//                return new NetworkImageHolderView();
//            }
//        },networkImages);

        //各种翻页效果
        transformerList.add(DefaultTransformer.class.getSimpleName());
        transformerList.add(AccordionTransformer.class.getSimpleName());
        transformerList.add(BackgroundToForegroundTransformer.class.getSimpleName());
        transformerList.add(CubeInTransformer.class.getSimpleName());
        transformerList.add(CubeOutTransformer.class.getSimpleName());
        transformerList.add(DepthPageTransformer.class.getSimpleName());
        transformerList.add(FlipHorizontalTransformer.class.getSimpleName());
        transformerList.add(FlipVerticalTransformer.class.getSimpleName());
        transformerList.add(ForegroundToBackgroundTransformer.class.getSimpleName());
        transformerList.add(RotateDownTransformer.class.getSimpleName());
        transformerList.add(RotateUpTransformer.class.getSimpleName());
        transformerList.add(StackTransformer.class.getSimpleName());
        transformerList.add(ZoomInTransformer.class.getSimpleName());
        transformerList.add(ZoomOutTranformer.class.getSimpleName());

        transforemerName = transformerList.get(13);
        try {
            cls = Class.forName("com.ToxicBakery.viewpager.transforms." + transforemerName);
            transforemer = (ABaseTransformer) cls.newInstance();
            homeBanner.getViewPager().setPageTransformer(true, transforemer);

            if (transforemerName.equals("StackTransformer")) {
                homeBanner.setScrollDuration(5000);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (java.lang.InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 出库历史查询
     */
    @OnClick(R.id.ckcx_ll)
    void Ckls() {
//        if (SharedPrefsUtil.getValue(getActivity(), "userInfo", "usertype", "").equals(SALE_CODE)) {
//            startActivity(new Intent(getActivity(), PreventFlee.class));
//        } else {
//            Intent intent = new Intent(getActivity(), OutLibraryHistory.class);
        Intent intent = new Intent(getActivity(), DlsLss.class);
        intent.putExtra("tag", "ckls");
        startActivity(intent);
//        }
    }

    /**
     * 退库历史
     */
    @OnClick(R.id.tkls_ll)
    void Tkls() {
//        Intent intent = new Intent(getActivity(), OutLibraryHistory.class);
        Intent intent = new Intent(getActivity(), DlsLss.class);
        intent.putExtra("tag", "tkls");
        startActivity(intent);
    }

    /**
     * 出库
     */
    @OnClick(R.id.ck_ll)
    void Ck() {
        Intent intent = new Intent(getActivity(), OutLibraryStepOne.class);
        intent.putExtra("tag", "ck");
        startActivity(intent);
    }

    /**
     * 退库
     */
    @OnClick(R.id.tk_ll)
    void Tk() {
        Intent intent = new Intent(getActivity(), OutLibraryStepOne.class);
        intent.putExtra("tag", "tk");
        startActivity(intent);
    }

    /**
     * 代理商，零售商
     */
    @OnClick(R.id.dls_ll)
    void DlsLss() {
        startActivity(new Intent(getActivity(), AgentRetailer.class));
    }

    /**
     * 调剂
     */
    @OnClick(R.id.tiaoji_ll)
    void TiaoJi() {
        Intent intent = new Intent(getActivity(), OutLibraryStepOne.class);
        intent.putExtra("tag", "tiaoji");
        startActivity(intent);
    }

    /**
     * 调剂历史
     */
    @OnClick(R.id.tiaojihis_ll)
    void TiaoJiHistory() {
        Intent intent = new Intent(getActivity(), DlsLss.class);
        intent.putExtra("tag", "tiaoji");
        startActivity(intent);
    }

    /**
     * 防窜查询
     */
    @OnClick(R.id.preventflee_ll)
    void PreventFlee() {
        startActivity(new Intent(getActivity(), PreventFlee.class));
    }


    /**
     * 离线出库历史
     */
    @OnClick(R.id.lxckls_ll)
    void OffLineCkls() {
        startActivity(new Intent(getActivity(), OffLineCkHistory.class));
    }

    /**
     * 离线出库
     */
    @OnClick(R.id.lxck_ll)
    void OffLineCk() {
        if (NetUtils.isConnected(getActivity())) {
            ShowToast.showShort(getActivity(), "网络正常，请使用在线出库");
        } else {
            startActivity(new Intent(getActivity(), OffLineCk.class));
        }
    }


    // 开始自动翻页
    @Override
    public void onResume() {
        super.onResume();
        //开始自动翻页
        homeBanner.startTurning(5000);
    }

    // 停止自动翻页
    @Override
    public void onStop() {
        super.onStop();
        //停止翻页
        homeBanner.stopTurning();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
