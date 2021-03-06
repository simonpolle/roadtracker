package be.ehb.roadtracker.presenters;

import android.content.Context;
import android.widget.Toast;

import be.ehb.roadtracker.config.ApiClient;
import be.ehb.roadtracker.config.ApplicationProperties;
import be.ehb.roadtracker.data.OAuthService;
import be.ehb.roadtracker.domain.AccessTokenResponse;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Simon Pollé on 15/03/2017.
 */

public class LoginPresenterImpl implements LoginPresenter
{

    private final Context context;
    private final LoginPresenterListener listener;
    private OAuthService service;

    public interface LoginPresenterListener
    {
        void authenticated(AccessTokenResponse response);

        void notAuthenticated();
    }

    public LoginPresenterImpl(Context context, LoginPresenterImpl.LoginPresenterListener listener)
    {
        this.context = context;
        this.listener = listener;
    }

    public void login(String email, String password)
    {
        service = ApiClient.getClient().create(OAuthService.class);

        service.getAccessToken("password", ApplicationProperties.getClientId(), ApplicationProperties.getClientSecret(), email, password)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<AccessTokenResponse>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Toast.makeText(context, e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                        listener.notAuthenticated();
                    }

                    @Override
                    public void onNext(AccessTokenResponse response)
                    {
                        ApplicationProperties.setAccessToken(response.access_token);
                        ApplicationProperties.setRefreshToken(response.refresh_token);
                        if (response != null)
                            listener.authenticated(response);
                        else
                            listener.notAuthenticated();
                    }
                });
    }
}
