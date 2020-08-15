package com.techkip.bebadriver.stupdstuff.Dev;

import android.app.Activity;
import android.widget.FrameLayout;

import com.techkip.bebadriver.R;
import com.vansuita.materialabout.builder.AboutBuilder;
import com.vansuita.materialabout.views.AboutView;


/**
 * Created by hillarie on 12/7/2017.
 */

public class DeveloperInfo {

    private Activity activity;
    private static int theme = R.style.AppThemeCustom;

    private DeveloperInfo(Activity activity) {
        this.activity = activity;
    }

    public static DeveloperInfo with(Activity activity){
        return new DeveloperInfo(activity);
    }

    public DeveloperInfo init(){
        activity.setTheme(theme);


        return this;
    }

    public void loadAbout() {
        final FrameLayout flHolder = (FrameLayout) activity.findViewById(R.id.about);

        AboutBuilder builder = AboutBuilder.with(activity)
                .setAppIcon(R.mipmap.ic_launcher)
                .setAppName(R.string.app_name)
                .setPhoto(R.drawable.meon)
                .setCover(R.drawable.mylogo)
                .setLinksAnimated(true)
                .setDividerDashGap(3)
                .setName("Hillarie Kip")
                .setSubTitle("Mobile App Developer")
                .setLinksColumnsCount(4)
                .setBrief("I'm  a Developer from Kenya doing both Mobile and Web Applications.")
                .addGooglePlayStoreLink("5396640207997518913")
                .addGitHubLink("Hillarie-kip")
                // .addBitbucketLink("jrvansuita")
                .addFacebookLink("hillarie.kip1")
                .addTwitterLink("_hillarie_kip")
                .addInstagramLink("hillarie_kip")
                .addGooglePlusLink("+LeeroyKush")
                .addLinkedInLink("hillarie-kip")
                /*.addYoutubeChannelLink("CaseyNeistat")
                .addDribbbleLink("user")*/
                .addEmailLink("kalyahillary@gmail.com")
                .addWhatsappLink("Hillarie", "+254727025739")
                .addSkypeLink("user")
                .addGoogleLink("user")
                .addAndroidLink("user")
                .addWebsiteLink("site")
                .addFiveStarsAction(R.string.uri_play_store_app)
                //.addMoreFromMeAction("Vansuita")
                .setVersionNameAsAppSubTitle()
                .addShareAction(R.string.app_name)
                .addUpdateAction(R.string.update_app)
                .setActionsColumnsCount(2)
                .addFeedbackAction("kalyahillary@gmail.com")
                /*.addPrivacyPolicyAction("http://www.docracy.com/2d0kis6uc2")
                .addIntroduceAction((Intent) null)
                .addHelpAction((Intent) null)
                .addChangeLogAction((Intent) null)
                .addRemoveAdsAction((Intent) null)
                .addDonateAction((Intent) null)*/
                .setWrapScrollView(true)
                .setNameColor(R.color.blue)
                .setIconColor(R.color.colorAccent)
                .setBriefColor(R.color.green)
                .setBackgroundColor(R.color.colorPrimary)
                .setShowAsCard(true);



        AboutView view = builder.build();

        flHolder.addView(view);
    }



}
